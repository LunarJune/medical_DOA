package xly;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.*;
import xly.doip.*;
import xly.doip.client.transport.*;
import xly.doip.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        TransportDoipClient client = new TransportDoipClient();
        ConnectionOptions connectionOptions = new ConnectionOptions();
        connectionOptions.address = "127.0.0.1";
        connectionOptions.port = 8888;
        DoipConnectionPool pool = new DoipConnectionPool(10,client,connectionOptions);
        DoipConnection conn1 = pool.get();
        DoipConnection conn2 = pool.get();
        pool.release(conn1);
        DoipConnection conn3 = pool.get();
        System.out.println(conn1.getSocket().getLocalPort());
        System.out.println(conn2.getSocket().getLocalPort());
        System.out.println(conn3.getSocket().getLocalPort());
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