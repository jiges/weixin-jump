package jiges.github;

import jiges.github.picture.Picture;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

/**
 * Created by ccr at 2018/1/8.
 */
public class PressClient {

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
//            picture.initImageViewer();
//            picture.loadPicture();
//            int loadCnt = 0;
            while(true) {
                picture.loadPicture();
                sendCommand(picture.analyze());

            }

        } catch (IOException | JadbException e) {
            e.printStackTrace();
        }
    }


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
