package xly.test;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;

/**
 * 客户端
 */
public class SocketTCP_Client {
    public static void main(String[] args) throws Exception {
        String ip = "127.0.0.1";
        int port = 8888;
        Socket socket = new Socket(InetAddress.getByName(ip), port);
        System.out.println("客户端 socket = " + socket.getClass());
        String message;
        Scanner scanner = new Scanner(System.in);
        OutputStream outputStream = socket.getOutputStream();
        for(int i = 0;i < 2;i++) {
            message = scanner.nextLine();
            outputStream.write(message.getBytes());
        }
        outputStream.close();
        socket.close();
//        System.out.println("客户端退出...");
    }

}
