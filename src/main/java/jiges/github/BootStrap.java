package jiges.github;

import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.List;

/**
 * 启动类
 * Created by ccr at 2018/1/3.
 */
public class BootStrap {

    public static void main(String args[]) {

        JadbConnection jadb = null;
        BufferedReader br = null;
        try {
            jadb = new JadbConnection();
            List<JadbDevice> devices = jadb.getDevices();
            JadbDevice device = devices.get(0);
            ServerSocket serverSocket = new ServerSocket(8899);
            while (true) {
                System.out.print("等待输入:");
                Socket client = serverSocket.accept();
                br = new BufferedReader(new InputStreamReader( new DataInputStream(client.getInputStream())));
                String input = br.readLine();
                System.out.println(input);
                double d = Double.valueOf(input);
//                double time = (d + 3) * 4.09;
                double time = d * 1.565;
                DecimalFormat df = new DecimalFormat("0");
                device.executeShell("input","touchscreen","swipe","200","200","200","200",String.valueOf(df.format(time)));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JadbException e) {
            e.printStackTrace();
        }

    }
}
