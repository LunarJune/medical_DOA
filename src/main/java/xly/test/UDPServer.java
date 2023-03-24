package xly.test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import xly.doip.DoipResponseHeaders;
import xly.doip.util.GsonUtility;

import java.net.*;

public class UDPServer {
    public static void main(String[] args) {
        DatagramSocket serverSocket = null;
        try {
            serverSocket = new DatagramSocket(9876);
            byte[] receiveData = new byte[1024];
            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                int length = receivePacket.getLength();
                String message = new String(receiveData, 0, length);
                InetAddress IPAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();
                JsonObject json = JsonParser.parseString(message).getAsJsonObject();
                System.out.println(json);
                DoipResponseHeaders header = new DoipResponseHeaders();
                header.status = "Status.001";
                JsonObject output = new JsonObject();
                output.addProperty("IPAddress", "127.0.0.1");
                output.addProperty("port", "8888");
                header.output = output;
                Gson gson = new Gson();
                String response = gson.toJson(header);
                byte[] sendData = response.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                serverSocket.send(sendPacket);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (serverSocket != null) {
                serverSocket.close();
            }
        }
    }
}
