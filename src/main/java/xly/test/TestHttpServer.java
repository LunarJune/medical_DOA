package xly.test;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import xly.doip.DoipResponseHeaders;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class TestHttpServer {
    public static final int length = 100000;
    static String msg = new String("");

    public static void main(String[] arg) throws Exception {
        for (int i = 0; i < length; i++) msg += "a";
        HttpServer server = HttpServer.create(new InetSocketAddress(8001), 0);
        server.createContext("/test", new TestHandler());
        server.start();
    }

    static class TestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            InputStream in = exchange.getRequestBody();
            InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
            JsonElement json = new JsonParser().parse(isr).getAsJsonObject();

            DoipResponseHeaders header = new DoipResponseHeaders();
            header.status = "OK";
            JsonObject out = new JsonObject();
            out.addProperty("msg", msg);
            header.output = out;
            exchange.sendResponseHeaders(200, 0);
            OutputStream os = exchange.getResponseBody();
            Gson gson = new Gson();
            os.write(gson.toJson(header).getBytes("UTF-8"));
            os.close();
        }
    }
}