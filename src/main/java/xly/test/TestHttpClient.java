package xly.test;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import xly.doip.DoipRequestHeaders;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import java.io.InputStream;
import java.io.IOException;

public class TestHttpClient {
    public static final int cnt = 1;

    public static void main(String[] args) throws IOException {
        String baseUrl = "http://127.0.0.1:8001/test";
        URL url = new URL(baseUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        Gson gson = new Gson();
        DoipRequestHeaders header = new DoipRequestHeaders();
        header.operationId = "Hello";
        header.clientId = "client";
        header.targetId = "target";

        long millis1 = System.currentTimeMillis();
        for (int i = 0; i < cnt; i++) {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            OutputStream os = connection.getOutputStream();
            os.write(gson.toJson(header).getBytes("UTF-8"));
            InputStream in = connection.getInputStream();
            InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
            JsonElement json = new JsonParser().parse(isr).getAsJsonObject();
            in.close();
        }
        long millis2 = System.currentTimeMillis();
        System.out.println(millis2 - millis1);//经过的毫秒数
        connection.disconnect();
    }
}
