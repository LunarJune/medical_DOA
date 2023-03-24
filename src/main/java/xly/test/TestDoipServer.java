package xly.test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import xly.doip.client.DigitalObject;
import xly.doip.client.Element;
import xly.doip.server.*;

import java.io.*;
import java.util.ArrayList;


public class TestDoipServer implements DoipProcessor {
    static String msg = new String("");

    @Override
    public void process(DoipServerRequest req, DoipServerResponse resp) throws IOException {
        resp.commit();
        DigitalObject dobj = new DigitalObject();
        Element el = new Element();
        el.id = "test";
        el.in = new ByteArrayInputStream(msg.getBytes());
        dobj.elements = new ArrayList<>();
        dobj.elements.add(el);
        Gson gson = new Gson();
        resp.getOutput().writeJson(gson.toJson(dobj));

        JsonObject elementSegmentJson = new JsonObject();
        elementSegmentJson.addProperty("id", el.id);
        resp.getOutput().writeJson(elementSegmentJson);
        resp.getOutput().writeBytes(el.in);
    }

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < TestHttpServer.length; i++) msg += "a";
        DoipServerConfig config = new DoipServerConfig();
        config.listenAddress = null;
        config.port = 8888;
        config.processorClass = TestDoipServer.class.getName();
        System.out.println(config.processorClass);
        DoipServer server = new DoipServer(config);
        server.init();
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
    }
}