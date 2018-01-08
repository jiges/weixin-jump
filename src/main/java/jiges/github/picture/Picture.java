package jiges.github.picture;

import jiges.github.PressClient;
import se.vidstige.jadb.*;

import javax.imageio.ImageIO;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * 图片工具，截图，展示和图片计算
 * Created by ccr at 2018/1/3.
 */
public class Picture {

    private static final int DEFAULT_WIDTH = 1080;

    private static final int DEFAULT_HEIGHT = 1920;

    //小人的RGB范围
    public static final Rgb LITTLE_MAN_RGB_MIN = new Rgb(43,43,70);
    public static final Rgb LITTLE_MAN_RGB_MAX = new Rgb(60,60,108);


    public static final int LITTLE_MAN_WIDTH = 60;

    private JadbDevice device;

    private BufferedImage image;

    private ImageViewer imageViewer;

    private int distance;

    public Picture(JadbDevice device) {
        this.device = device;
    }

    //图片的展示
    public void initImageViewer() {
        this.imageViewer = new ImageViewer(DEFAULT_WIDTH,DEFAULT_HEIGHT);
        this.imageViewer.getRefreshBtn().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadPicture();
                imageViewer.setComputeRet(String.valueOf(analyze()));
            }
        });
        this.imageViewer.getComputeBtn().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                imageViewer.setComputeRet(String.valueOf(analyze()));
            }
        });
        this.imageViewer.getSendBtn().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PressClient.sendCommand(distance);
            }
        });
        this.imageViewer.setVisible(true);
    }


    /**
     * adb截屏，将图片加载到pc中进行分析
     */
    public void loadPicture(){
        ByteArrayOutputStream outputStream = null;
        try {
            device.executeShell("mkdir","/sdcard/screenshot/");
            device.executeShell("/system/bin/screencap", "-p", "/sdcard/screenshot/screenshot.png");
            //截图只是发送命令，手机需要一定时间需要完成截屏动作，直接拉去图片可能会报错
            TimeUnit.SECONDS.sleep(2);
            outputStream =  new ByteArrayOutputStream();
            device.pull(new RemoteFile("/sdcard/screenshot/screenshot.png"), outputStream);
            if(outputStream.size() > 0) {
                image = ImageIO.read(new ByteArrayInputStream(outputStream.toByteArray()));
                if(null != imageViewer)
                    imageViewer.refreshImage(image);
            }
        } catch (IOException | JadbException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 图片分析
     * 1、计算小人的位置，取x坐标。
     * 2、计算下一跳方块或圆盘的顶点位置，取x坐标。
     * 3、根据x坐标差计算按压时间
     */
    public int analyze(){
        Point littleManPos = this.computeLittleManPosition();
        Point nextPos = computeNextPosition(littleManPos);
        this.distance = Math.abs(nextPos.getX() - littleManPos.getX());
        System.out.println(this.distance);
        return distance;
    }

    /**
     * 计算小人的位置，算法：
     * 1、小人的位置总在屏幕一定位置出现，为了减少计算量，假设小人总是在屏幕的1/3到2/3的位置出现。
     * 2、给小人做横切线，底部最长横切线的中点即为小人的位置。
     * 3、根据颜色判断，找出这条线。
     * @return 位置
     */
    public Point computeLittleManPosition(){
        int imageWidth = image.getWidth();
        int imageHeight  = image.getHeight();
        //记录所有符合条件的线段
        java.util.List<Point[]> lines = new ArrayList<>();
        //逐行扫描
        for(int j = imageHeight / 3;j < imageHeight * 2 / 3 ;j++) {
            Point[] line = new Point[]{new Point(0,j),new Point(0,j)};
            for(int i = image.getMinX();i < imageWidth ;i++) {
                //该rgb大小4个字节，从高位到底位分别存放alpha，red，green，blue。
                int rgb = image.getRGB(i,j);
                //如果颜色在小人颜色范围内，则记录首尾点
                if(new Rgb((rgb >> 16) & 0xff ,(rgb >> 8) & 0xff,rgb & 0xff).isBetween(LITTLE_MAN_RGB_MIN, LITTLE_MAN_RGB_MAX)) {
                    line[1].setX(i);
                } else {
                    if(line[1].getX() > line[0].getX()) {
                        lines.add(new Point[]{new Point(line[0].getX(),line[0].getY()),new Point(line[1].getX(),line[1].getY())});
                    }
                    line[0].setX(i);
                    line[1].setX(i);
                }
            }
        }
        //找出最长的一条
        Point[] line = new Point[]{new Point(0,0),new Point(0,0)};
        for (Point[] item : lines) {
            if(Math.abs(item[1].getX() - item[0].getX()) > Math.abs(line[1].getX() - line[0].getX())) {
                line = item;
            }
        }
        Point p = new Point((line[0].getX() + line[1].getX()) / 2,(line[0].getY() + line[1].getY()) / 2);
        if(null != imageViewer)
            imageViewer.showPoint(p,image);
//        System.out.println(p);
        //返回中点
        return p;
    }

    /**
     * 找出小人下一跳的位置
     * 1、下一跳的方块或圆块的位置也总是出现在屏幕的1/3到2/3的位置。
     * 2、根据底色跟方块的色差，找到方块或者圆盘的顶点。
     * 3、可以根据顶点的x坐标和小人的x坐标算出按压时间
     * @param littleMan 小人的位置，为了解决小人的高度超过方块的高度引起的BUG
     * @return 位置
     */
    public Point computeNextPosition(Point littleMan){
        int imageWidth = image.getWidth();
        int imageHeight  = image.getHeight();
        Rgb bgRgb1 = null,bgRgb2 = null;
        Point point = null;
        for(int j = imageHeight / 3;j < imageHeight * 2 / 3 ;j++) {
            //顶点有可能不是一个点，而是一个小线段
            Point[] line = new Point[]{new Point(0,j),new Point(0,j)};

            for(int i = image.getMinX();i < imageWidth ;i++) {
                //解决小人的高度超过方块的高度引起的BUG
                //以小人的x坐标为终点，左右LITTLE_MAN_WIDTH像素的位置不扫描
                if(i > littleMan.getX() - LITTLE_MAN_WIDTH && i < littleMan.getX() + LITTLE_MAN_WIDTH) {
                    continue;
                }
                //该rgb大小4个字节，从高位到底位分别存放alpha，red，green，blue。
                int rgb = image.getRGB(i,j);
                //记录底色的rgb值,底色并不是纯色，所以底色是一个范围，设范围在20以内
                if(i == image.getMinX()) {
                    bgRgb1 = new Rgb(((rgb >> 16) & 0xff) - 10 ,((rgb >> 8) & 0xff) - 10,(rgb & 0xff) - 10);
                    bgRgb2 = new Rgb(((rgb >> 16) & 0xff) + 10 ,((rgb >> 8) & 0xff) + 10,(rgb & 0xff) + 10);
                }
                //如果颜色在小人颜色范围内，则记录首尾点
                if(!new Rgb((rgb >> 16) & 0xff ,(rgb >> 8) & 0xff,rgb & 0xff).isBetween(bgRgb1,bgRgb2)) {
                    //解决小人高于方块时的bug
                    line[1].setX(i);
                } else {
                    if(line[1].getX() > line[0].getX()) {
                        break;
                    }
                    line[0].setX(i);
                    line[1].setX(i);
                }
            }
//            System.out.println(line[0] + "," + line[1]);
            if(line[1].getX() > line[0].getX()) {
                point = new Point((line[0].getX() + line[1].getX()) / 2,(line[0].getY() + line[1].getY()) / 2);
                break;
            }
        }
        //标记绿色
        if(null != imageViewer)
            imageViewer.showPoint(point,image);
//        System.out.println(point);
        return point;
    }

}
