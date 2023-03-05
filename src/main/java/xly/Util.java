package xly;

import java.io.IOException;
import java.io.InputStream;

public class Util {
    public static void printStream(InputStream in) throws IOException {
        int n;
        // 读取文件，并将内容转换为字符输出
        while ((n = in.read()) != -1){
            System.out.print((char)n);
        }
    }
}
