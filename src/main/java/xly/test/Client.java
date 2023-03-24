package xly.test;

import xly.Util;

import java.io.*;
import java.net.*;

/**
 * 测试用的Client类
 */
public class Client implements AutoCloseable {
    public Socket socket;
    private InputStream in;
    private OutputStream out;

    public Client(String ip, int port) throws IOException {
        socket = new Socket(InetAddress.getByName(ip), port);
//        System.out.println("客户端连接成功" + socket.getInetAddress());
        in = socket.getInputStream();
        out = socket.getOutputStream();
    }

    public void beginMonitorRead() {
        new Thread(this::read).start();
    }

    public void read() {
        try {
            Util.printStream(in);
        } catch (IOException e) {
        }
    }

    public String readAsString() {
        int n;
        String str = new String();
        try {
            while ((n = in.read()) != -1) {
                str += (char) n;
            }
        } catch (IOException e) {
            //pass
        }
        return str;
    }

    public void write(byte[] bytes) throws IOException {
        out.write(bytes);
    }

    public void close() throws IOException {
//        System.out.println("客户端关闭");
        if (in != null) in.close();
        if (out != null) out.close();
        socket.close();
    }
}
