package xly.test;

import java.io.IOException;
import java.util.Scanner;


/**
 * 多个client同时向服务器发出请求，得到响应
 */
public class Test_Client2 {
    public static void main(String[] args) throws Exception {
        String ip = "127.0.0.1";
        int port = 8888;
        new Thread(new ClientRunnable(ip, port, "###client1")).start();
        Thread.sleep(100);
        new Thread(new ClientRunnable(ip, port, "###client2")).start();
    }

    static class ClientRunnable implements Runnable {
        private String ip;
        private int port;
        private String name;

        public ClientRunnable(String ip, int port, String name) {
            this.ip = ip;
            this.port = port;
            this.name = name;
        }

        @Override
        public void run() {
            try (Client client = new Client(ip, port)) {
                System.out.println("发出请求"+name);
                client.write(name.getBytes());
                client.read();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

