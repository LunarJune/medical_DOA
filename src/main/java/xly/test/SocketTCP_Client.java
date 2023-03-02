package xly.test;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * 客户端
 */
public class SocketTCP_Client {
    public static void main(String[] args) throws Exception {
        //1. 连接服务端 (ip , 端口）
        //解读:连接本机的 9999端口, 如果连接成功，返回Socket对象
        Socket socket = new Socket(InetAddress.getByName("10.68.63.156"), 9999);
        System.out.println("客户端 socket = " + socket.getClass());
        //2. 连接上后，生成Socket, 通过socket.getOutputStream()
        //得到和 socket对象关联的输出流对象
        OutputStream outputStream = socket.getOutputStream();
        //3. 通过输出流，写入数据到 数据通道
        outputStream.write("Hello,server".getBytes());
        //4. 关闭流对象和socket, 必须关闭
        outputStream.close();
        socket.close();
        System.out.println("客户端退出...");
    }

}
