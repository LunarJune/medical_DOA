package xly.test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.eclipse.jetty.util.IO;
import xly.doip.DoipRequestHeaders;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.CountDownLatch;

public class UDPClient {
    static int clientCnt = 10;
    static volatile CountDownLatch cdl;

    public static void main(String[] args) throws InterruptedException {
        long millis1 = System.currentTimeMillis();
        cdl = new CountDownLatch(clientCnt);
        for (int i = 0; i < clientCnt; i++) {
            new UDPClientThread().start();
        }
        cdl.await();
        long millis2 = System.currentTimeMillis();
        System.out.println(millis2 - millis1);//经过的毫秒数
    }

    static class UDPClientThread extends Thread {
        @Override
        public void run() {
            try {
                DatagramSocket clientSocket = new DatagramSocket();
                InetAddress IPAddress = InetAddress.getByName("localhost");
                DoipRequestHeaders header = new DoipRequestHeaders();
                header.clientId = "client";
                header.operationId = "Op.GetLHS";
                header.targetId = "test/1.2.66";
                Gson gson = new Gson();
                String message = gson.toJson(header);
                byte[] sendData = message.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
                clientSocket.send(sendPacket);
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                clientSocket.receive(receivePacket);
                int length = receivePacket.getLength();
                String response = new String(receiveData, 0, length);
                JsonObject json = JsonParser.parseString(response).getAsJsonObject();
                System.out.println(json);
                clientSocket.close();
                cdl.countDown();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
