package xly;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import com.google.gson.*;
import xly.doip.*;

public class Main {
    public static void main(String[] args) throws Exception {
        String str = "{\"a\":\"b\nb\nb\"}";
        Gson gson = new Gson();

//        OutputStream out = new FileOutputStream("tmp.txt");
//        OutDoipMessage outd = new OutDoipMessageImpl(out);
//        JsonObject json = new JsonObject();
//        json.addProperty("a","b");
//        outd.writeJson(json);
//        outd.writeBytes("#\n#\n#".getBytes());
//        outd.close();
//        InputStream in = new FileInputStream("tmp.txt");
//        InDoipMessage ind = new InDoipMessageImpl(in);
//        for(InDoipSegment seg: ind){
//            Util.printStream(seg.getInputStream());
//        }
    }
}