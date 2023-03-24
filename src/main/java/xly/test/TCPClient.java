package xly.test;

import xly.doip.InDoipSegment;
import xly.doip.client.*;
import xly.doip.client.transport.DoipClientResponse;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class TCPClient {
    static int clientCnt = 10;
    static volatile CountDownLatch cdl;

    public static void main(String[] args) throws IOException, DoipException, InterruptedException {
        long millis1 = System.currentTimeMillis();
        cdl = new CountDownLatch(clientCnt);
        for (int i = 0; i < clientCnt; i++) {
            new TCPClientThread().start();
        }
        cdl.await();
        long millis2 = System.currentTimeMillis();
        System.out.println(millis2 - millis1);//经过的毫秒数
    }

    static class TCPClientThread extends Thread {
        @Override
        public void run() {
            try {
                DoipClient client = new DoipClient();
                ServiceInfo serviceInfo = new ServiceInfo("test/1.2.66", "127.0.0.1", 8888);
                AuthenticationInfo authInfo = new TokenAuthenticationInfo("client", null);
                DoipClientResponse result = client.getLHS("test/1.2.66", authInfo, serviceInfo);
//                System.out.println(result.getStatus());
//                for (InDoipSegment i : result.getOutput()) System.out.println(i.getJson());
                client.close();
                cdl.countDown();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
