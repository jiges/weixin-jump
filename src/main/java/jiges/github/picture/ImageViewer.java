package jiges.github.picture;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * 图片展示，并取点分析
 * Created by ccr at 2018/1/3.
 */
public class ImageViewer  extends JFrame {

    //记录鼠标点击第一下的位置
    private JTextField xPoint;
    //记录鼠标点击第二下的位置
    private JTextField yPoint;
    //计算的距离
    private JTextField xyDistance;

    private JLabel imageLabel;

    private int clickCnt = 0;

    private Point x;

    private Point y;

    private int width;

    private int height;

    private String imageUrl;

    private JButton refreshBtn;
    private JButton computeBtn;
    private JButton sendBtn;
    private JTextField computeRet;

    public ImageViewer(int width,int height) {
        this.width = width;
        this.height = height;

        JPanel panel=new JPanel(new BorderLayout());
        JPanel panel2=new JPanel();
        JPanel panel3=new JPanel();

//        String urlString="C:\\Users\\ccr\\AppData\\Local\\Temp\\screenshot.png";
//        ImageIcon imageIcon = new ImageIcon(url);
//        imageIcon.setImage(imageIcon.getImage().getScaledInstance(width, height,Image.SCALE_DEFAULT));
        this.imageLabel=new JLabel();

        JLabel x = new JLabel("x点:");
        this.xPoint = new JTextField(5);
        JLabel y = new JLabel("y点:");
        this.yPoint = new JTextField(5);
        JLabel ret = new JLabel("距离:");
        this.xyDistance = new JTextField(4);
        this.refreshBtn = new JButton("刷新");
        this.computeBtn = new JButton("计算");
        this.sendBtn = new JButton("发送");
        computeRet = new JTextField(4);



        panel.add(this.imageLabel,BorderLayout.CENTER);
        panel2.add(x);
        panel2.add(xPoint);
        panel2.add(y);
        panel2.add(yPoint);
        panel2.add(ret);
        panel2.add(xyDistance);
        panel2.add(refreshBtn);
        panel3.add(computeBtn);
        panel3.add(computeRet);
        panel3.add(sendBtn);

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(panel,BorderLayout.CENTER);
        this.getContentPane().add(panel2,BorderLayout.SOUTH);
        this.getContentPane().add(panel3,BorderLayout.NORTH);

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setTitle("显示图像");
        this.setVisible(true);
        this.setSize(width / 3,height / 3 + 100);
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(ImageViewer.this.clickCnt++ % 2 == 0) {
                    ImageViewer.this.x = new Point(e.getX(),e.getY());
                    ImageViewer.this.xPoint.setText(ImageViewer.this.x.toString());
                } else {
                    ImageViewer.this.y = new Point(e.getX(),e.getY());
                    ImageViewer.this.yPoint.setText(ImageViewer.this.y.toString());
                }

                if(null != ImageViewer.this.x && null != ImageViewer.this.y) {
                    double d = Point.computeDistance(ImageViewer.this.x,ImageViewer.this.y);
                    DecimalFormat df = new DecimalFormat("0.000");
                    ImageViewer.this.xyDistance.setText(String.valueOf(df.format(d)));
                }
            }
        });
    }

    public void refreshImage(String url){
        this.imageUrl = imageUrl;
        ImageIcon imageIcon = new ImageIcon(url);
        imageIcon.setImage(imageIcon.getImage().getScaledInstance(width, height,Image.SCALE_DEFAULT));
        imageLabel.setIcon(imageIcon);
    }

    public void refreshImage(Image image){
        ImageIcon imageIcon = new ImageIcon(image);
        imageIcon.setImage(imageIcon.getImage().getScaledInstance(width, height,Image.SCALE_DEFAULT));
        imageLabel.setIcon(imageIcon);
    }

    public void setComputeRet(String ret){
        this.computeRet.setText(ret);
    }

    public JButton getRefreshBtn() {
        return refreshBtn;
    }

    public JButton getComputeBtn() {
        return computeBtn;
    }

    public JButton getSendBtn() {
        return sendBtn;
    }

    public static void main(String[] args) throws IOException {
        String urlString="C:\\Users\\ccr\\AppData\\Local\\Temp\\screenshot.png";
        ImageViewer showImage=new ImageViewer(1080,1920);
        BufferedImage image = ImageIO.read(new FileInputStream(urlString));
        int imageWidth = image.getWidth();
        int imageHeight  = image.getHeight();
        Rgb bgRgb1 = null,bgRgb2 = null;
        Point point = null;
        Point littleman = new Point(683,0);
        for(int j = imageHeight / 3;j < imageHeight * 2 / 3 ;j++) {
            //顶点有可能不是一个点，而是一个小线段
            Point[] line = new Point[]{new Point(0,j),new Point(0,j)};
            for(int i = image.getMinX();i < imageWidth ;i++) {
                if(i > littleman.getX() - 60 && i < littleman.getX() + 60) {
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
        showImage.showPoint(point,image);
        System.out.println(image.getWidth());
    }

    public void showPoint(Point point,BufferedImage image) {
        //标记白色
        for (int i = point.getX() - 5; i <= point.getX() + 5; i++) {
            for (int j = point.getY() - 5; j <= point.getY() +5; j++) {
                image.setRGB(i,j,0xFF00FF00);
            }
        }

        this.refreshImage(image);
    }
}
