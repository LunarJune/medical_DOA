package xly.test;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.*;


/**
 * 多个client同时向服务器发出请求，得到响应
 */
public class Test_Client2 {
    static int clientCnt = 10;
    static volatile CountDownLatch cdl;

    public static void main(String[] args) throws Exception {
        String ip = "127.0.0.1";
        int port = 1234;
        while (true) {
            long millis1 = System.currentTimeMillis();
            cdl = new CountDownLatch(clientCnt);
            for (int i = 0; i < clientCnt; i++)
                new Thread(new ClientRunnable(ip, port, "client")).start();
            cdl.await();
            long millis2 = System.currentTimeMillis();
            System.out.println(millis2 - millis1);//经过的毫秒数
        }
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
                client.write(name.getBytes());
                String res = client.readAsString();
                System.out.println(res);
                cdl.countDown();
            } catch (IOException e) {
//                System.out.println(e.getMessage());
//                System.exit(0);
                throw new RuntimeException(e);
            }
        }
    }
}

