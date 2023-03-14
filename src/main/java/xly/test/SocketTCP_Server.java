package xly.test;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 服务器端
 */
public class SocketTCP_Server {
    public static void main(String[] args) throws Exception {
        int port = 8888;
        System.out.printf("服务器段，监听%d端口，等待连接 ", port);
        ServerSocket serverSocket = new ServerSocket(port);
        Socket socket = serverSocket.accept();
        System.out.println("服务器端 socket = " + socket.getClass());
        InputStream inputStream = socket.getInputStream();
        byte[] buf = new byte[1024];
        int readLine = 0;
        while (true) {
            while ((readLine = inputStream.read(buf)) != -1) {
                System.out.println(new String(buf, 0, readLine));
            }
        }
//        inputStream.close();
//        socket.close();
//        serverSocket.close();
    }
}
