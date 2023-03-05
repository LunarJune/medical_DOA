package xly.LHS;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import xly.doip.*;
import xly.doip.client.DigitalObject;
import xly.doip.client.Element;
import xly.doip.server.*;
import xly.Util;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;


public class LocalServer implements DoipProcessor {
    private static class Test {
        public String tt = "hhhh";
    }

    @Override
    public void process(DoipServerRequest req, DoipServerResponse resp) throws IOException {
        System.out.println("hhhh");
        resp.commit();
        for (InDoipSegment segment : req.getInput()) {
            if (segment.isJson()) {
                var json = segment.getJson();
                System.out.println(json);
            }else {
                System.out.print("stream ");
                InputStream in = segment.getInputStream();
//                FileOutputStream fos = new FileOutputStream("doip_test.pdf");
//                byte[] b = new byte[1024];
//                while ((in.read(b)) != -1) {
//                    fos.write(b);// 写入数据
//                }
                Util.printStream(in);
                System.out.println("");
                in.close();
//                fos.close();// 保存数据
            }
        }
        DigitalObject dobj = new DigitalObject();
        Element el = new Element();
        el.id = "from_server";
//        el.in = Files.newInputStream(Paths.get("DO-IRP.pdf"));
        el.in = new ByteArrayInputStream("from server".getBytes());
        dobj.elements = new ArrayList<>();
        dobj.elements.add(el);
        Gson gson = new Gson();
        resp.getOutput().writeJson(gson.toJson(dobj));

        JsonObject elementSegmentJson = new JsonObject();
        elementSegmentJson.addProperty("id", el.id);
        resp.getOutput().writeJson(elementSegmentJson);
        resp.getOutput().writeBytes(el.in);
//        var out = resp.getOutput().getBytesOutputStream();
//        out.write("{\"a\":\"b\"}".getBytes(StandardCharsets.UTF_8));
//        out.flush();
//        try {
//            resp.commit();
//            for (InDoipSegment segment : req.getInput()) {
//                if (segment.isJson()) {
////                    var json = segment.getJson();
////                    System.out.println(json);
//                    resp.getOutput().writeJson(segment.getJson());
//                } else {
//                    try (
//                            InputStream in = segment.getInputStream();
//                            OutputStream out = resp.getOutput().getBytesOutputStream();
//                    ) {
//                        byte[] buf = new byte[8192];
//                        int r;
//                        while ((r = in.read(buf)) > 0) {
//                            out.write(buf, 0, r);
//                        }
//                    }
//                }
//            }
//        } catch (UncheckedIOException e) {
//            throw e.getCause();
//        }
    }

    public static void main(String[] args) throws Exception {
        DoipServerConfig config = new DoipServerConfig();
        config.listenAddress = null;
        config.port = 8888;
        config.processorClass = LocalServer.class.getName();
        System.out.println(LocalServer.class.getName());
        DoipServer server = new DoipServer(config);
        server.init();
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
    }
}