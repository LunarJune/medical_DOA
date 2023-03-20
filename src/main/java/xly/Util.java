package xly;

import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public class Util {
    public static void printStream(InputStream in) throws IOException {
        int n;
        // 读取文件，并将内容转换为字符输出
        while ((n = in.read()) != -1) {
            System.out.print((char) n);
        }
    }

    public static void printBuffer(ByteBuffer buffer) {
        buffer.flip();
        while (buffer.hasRemaining()) {
            System.out.println((char) buffer.get());
        }
        buffer.rewind();
    }
}
