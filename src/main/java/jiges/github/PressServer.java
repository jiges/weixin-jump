package jiges.github;

import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 模拟按压服务
 * Created by ccr at 2018/1/3.
 */
public class PressServer {

    public static void main(String args[]) {

        JadbConnection jadb = null;
        DataInputStream reader = null;
        DataOutputStream writer = null;
        try {
            jadb = new JadbConnection();
            List<JadbDevice> devices = jadb.getDevices();
            JadbDevice device = devices.get(0);
            ServerSocket serverSocket = new ServerSocket(8899);
            while (true) {
                System.out.print("等待输入:");
                Socket client = serverSocket.accept();
                reader = new DataInputStream(client.getInputStream());
                writer = new DataOutputStream(client.getOutputStream());
                String input = reader.readUTF();
                System.out.println(input);
                int d = Integer.valueOf(input);
//                double time = (d + 3) * 4.09;
                //计算按压时间
                double time = d * 1.565;
                DecimalFormat df = new DecimalFormat("0");
                //发送按压指令
                device.executeShell("input","touchscreen","swipe","200","200","200","200",String.valueOf(df.format(time)));
                //睡眠一定时间后才返回，需要完成跳跃动作
                TimeUnit.MILLISECONDS.sleep((long) time + 2000);
                writer.writeUTF("OK");
            }

        } catch (IOException | JadbException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}
