package xly.LHS;

import com.google.gson.Gson;
import xly.doip.*;
import xly.doip.server.*;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;


public class LocalServer implements DoipProcessor {
    private static class Test {
        public String tt = "hhhh";
    }

    @Override
    public void process(DoipServerRequest req, DoipServerResponse resp) throws IOException {
        System.out.println("hhhh");
        for (InDoipSegment segment : req.getInput()) {
            if (segment.isJson()) {
                var json = segment.getJson();
                System.out.println(json);
            }else {
                System.out.println("stream");
                InputStream in = segment.getInputStream();
                FileOutputStream fos = new FileOutputStream("doip_test.pdf");
                byte[] b = new byte[1024];
                while ((in.read(b)) != -1) {
                    fos.write(b);// 写入数据
                }
                in.close();
                fos.close();// 保存数据
            }
        }
        resp.commit();
        Gson gson = new Gson();
        Test test = new Test();
        resp.getOutput().writeJson(gson.toJson(test));
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
        config.listenAddress = "127.0.0.1";
        config.port = 8888;
        config.processorClass = LocalServer.class.getName();
        System.out.println(LocalServer.class.getName());
        DoipServer server = new DoipServer(config);
        server.init();
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
    }
}