package xly.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.*;

public class ThreadPoolServer {
    static int numThreads = 200;
    static int calTimes = 10;//计算次数，单位（万次）
    static ExecutorService execServ;
    static ServerSocket serverSocket;

    static void handle(Socket socket) {
        try {
            OutputStream out = socket.getOutputStream();
            double num = 1;
            for (int i = 0; i < calTimes * 10000; i++) num += Math.random();
            out.write(("hello" + num).getBytes());
            out.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            socket.close();
        } catch (Exception e) {
            // ignore
        }
    }

    public static void main(String[] args) throws IOException {
        serverSocket = new ServerSocket(1234);
        execServ = Executors.newFixedThreadPool(numThreads);
        new Thread(() -> {
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    execServ.execute(() -> handle(socket));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}
