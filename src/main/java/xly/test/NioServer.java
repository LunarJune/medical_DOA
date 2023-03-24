package xly.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class NioServer {

    private static final int BUFFER_SIZE = 100;
    private static final int TIMEOUT = 3000;
    private static Map<SocketChannel, String> map = new HashMap<>();

    public static void main(String[] args) throws IOException, InterruptedException {

        // 创建ServerSocketChannel并绑定端口
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(1234));
        serverSocketChannel.configureBlocking(false);

        // 创建Selector并将ServerSocketChannel注册到Selector上
        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();

                if (key.isAcceptable()) {
                    // 客户端请求连接
                    SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(BUFFER_SIZE));
//                    System.out.println("客户端连接成功：" + socketChannel.getRemoteAddress());
                } else if (key.isReadable()) {
                    // 读取客户端发送的数据
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    try {
                        int bytesRead = socketChannel.read(buffer);
                        String req = "";
                        while (true) {
                            if (bytesRead == -1) {
                                socketChannel.close();
                                break;
                            } else if (bytesRead > 0) {
                                buffer.flip();
                                byte[] data = new byte[buffer.remaining()];
                                buffer.get(data);
                                req = req + new String(data);
                                buffer.clear();
                                bytesRead = socketChannel.read(buffer);
                            } else {
                                map.put(socketChannel, req);
//                                System.out.println("收到请求 " + req);
                                key.interestOps(SelectionKey.OP_WRITE);
                                break;
                            }
                        }
                    } catch (SocketException e) {
//                        System.out.println("remove!");
                        socketChannel.close();
                        key.cancel();
                    }
                } else if (key.isWritable()) {
                    double num = 1;
                    for (int i = 0; i < ThreadPoolServer.calTimes * 10000; i++) num += Math.random();
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    String req = map.get(socketChannel);
//                    System.out.println(req + "发送数据");
                    ByteBuffer buffer = ByteBuffer.wrap(("hello" + num).getBytes());
                    socketChannel.write(buffer);
                    socketChannel.close();
                    key.cancel();
                }
                keyIterator.remove();
            }
        }
    }
}
