package xly;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import com.google.gson.*;

public class Main {
    private static class Test{
        public String tt = "hhhh";
    }
    public static void main(String[] args) throws Exception {
        Gson gson = new Gson();
        Test test = new Test();
        System.out.println(gson.toJson(test));
    }
}