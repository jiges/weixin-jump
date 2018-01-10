package jiges.github;

import jiges.github.picture.Picture;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;

import java.io.*;
import java.net.Socket;
import java.util.List;

/**
 * Created by ccr at 2018/1/8.
 */
public class PressClient {

    /**
     * 图片分析
     */
    public static void main(String[] args) {
        JadbConnection jadb = null;
        try {
            jadb = new JadbConnection();
            List<JadbDevice> devices = jadb.getDevices();
            if(null == devices || devices.isEmpty()) {
                System.out.println("未找到设备.");
                System.exit(0);
            }
            JadbDevice device = devices.get(0);
            Picture picture = new Picture(device);
            while(true) {
                picture.loadPicture();
                sendCommand(picture.analyze());

            }

        } catch (IOException | JadbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 手动输入距离
     */
    /*public static void main(String[] args) {
        BufferedReader br = null;
        try {
            while(true) {
                //等待键盘输入
                br = new BufferedReader(new InputStreamReader(System.in));
                System.out.print("请输入距离:");
                String input = br.readLine();
                if(null != input && input.equals("quit")) {
                    System.exit(0);
                }
                sendCommand(Integer.valueOf(input));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/


    /**
     * 发送按压命令，可将Socket提出来，不需要每次都建立连接
     * @param distance 距离
     */
    public static void sendCommand(int distance) {
        Socket socket = null;
        try {
            socket = new Socket("localhost",8899);
            socket.setSoTimeout(10000);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());
            out.writeUTF(String.valueOf(distance));
            String res = in.readUTF();
            System.out.println(res);
        } catch (IOException e1) {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e2) {
                    socket = null;
                }
            }
            e1.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e1) {
                    socket = null;
                }
            }
        }
    }
}
