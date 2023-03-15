package xly;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.*;
import xly.doip.*;
import xly.doip.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        int b = 1;
        long c = (long) b;
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