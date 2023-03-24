package xly.test;

import xly.doip.client.*;

import java.io.IOException;

public class TestDoipClient {

    public static void main(String[] args) throws IOException, DoipException, InterruptedException {
        DoipClient client = new DoipClient();
        AuthenticationInfo authInfo = new TokenAuthenticationInfo("clientid1", "token");
        ServiceInfo serviceInfo = new ServiceInfo("TEST", "127.0.0.1", 8888);

        long millis1 = System.currentTimeMillis();
        for (int i = 0; i < TestHttpClient.cnt; i++) {
            DigitalObject result = client.hello("TEST", authInfo, serviceInfo);
        }
        long millis2 = System.currentTimeMillis();
        System.out.println(millis2 - millis1);//经过的毫秒数
        client.close();
    }
}
