package xly.test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;


/**
 * 不断在终端输入，不断发送
 */
public class Test_Client1 {
    public static void main(String[] args) throws Exception {
        String ip = "127.0.0.1";
        int port = 1234;
        try (Client client = new Client(ip, port)) {
            client.beginMonitorRead();
            String message;
            Scanner scanner = new Scanner(System.in);
            while (true) {
                message = scanner.nextLine();
                if(message.equals("stop")) break;
                client.write(message.getBytes());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
