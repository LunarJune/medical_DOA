import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 服务器端
 */
public class SocketTCP_Server {
    public static void main(String[] args) throws Exception {
        //思路
        //1. 在本机 的9999端口监听, 等待连接
        //   细节: 要求在本机没有其它服务在监听9999
        //   细节：这个 ServerSocket 可以通过 accept() 返回多个Socket[多个客户端连接服务器的并发]
        int port = 8888;
        ServerSocket serverSocket = new ServerSocket(port);
//        serverSocket.bind(new InetSocketAddress(9999), 50);
        System.out.printf("服务器段，监听%d端口，等待连接",port);
        //2. 当没有客户端连接9999端口时，程序会 阻塞, 等待连接
        //   如果有客户端连接，则会返回Socket对象，程序继续
        Socket socket = serverSocket.accept();
        System.out.println("服务器端 socket = " + socket.getClass());
        //3. 通过socket.getInputStream() 读取客户端写入到数据通道的数据, 显示
        InputStream inputStream = socket.getInputStream();
        //4. IO读取
        byte[] buf = new byte[1024];
        int readLine = 0;
        while ((readLine = inputStream.read(buf)) != -1) {
            System.out.println(new String(buf, 0, readLine));
        }
        //5.关闭IO流和socket
        inputStream.close();
        socket.close();
        serverSocket.close();
    }
}
