package xly.test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import xly.doip.client.DigitalObject;
import xly.doip.client.Element;
import xly.doip.server.*;

import java.io.*;
import java.util.ArrayList;

public class TCPServer implements DoipProcessor {
    static String msg = new String("");

    @Override
    public void process(DoipServerRequest req, DoipServerResponse resp) throws IOException {
//        System.out.println(req.getClientId());
//        System.out.println(req.getTargetId());
//        System.out.println(req.getOperationId());
        JsonObject output = new JsonObject();
        output.addProperty("IPAddress", "127.0.0.1");
        output.addProperty("port", "8888");
        resp.writeCompactOutput(output);
    }

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < TestHttpServer.length; i++) msg += "a";
        DoipServerConfig config = new DoipServerConfig();
        config.listenAddress = null;
        config.port = 8888;
        config.processorClass = TCPServer.class.getName();
        System.out.println(config.processorClass);
        DoipServer server = new DoipServer(config);
        server.init();
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
    }
}
