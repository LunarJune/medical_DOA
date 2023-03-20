package xly;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.*;
import xly.doip.*;
import xly.doip.client.*;
import xly.doip.client.transport.*;
import xly.doip.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        DoipRequestHeaders headers = new DoipRequestHeaders();
        headers.clientId = "client";
        headers.targetId = "";
        headers.operationId = "Op.GetLHS";
        DoipRequestHeadersWithRequestId headersWithRequestId = new DoipRequestHeadersWithRequestId(headers);
        headersWithRequestId.requestId = UUID.randomUUID().toString();
        String json = GsonUtility.getGson().toJson(headersWithRequestId);
        byte[] tmp = json.getBytes(StandardCharsets.UTF_8);
        System.out.println(new String(tmp).length());
        DoipResponseHeadersWithRequestId headers1 = new DoipResponseHeadersWithRequestId();
        headers1.status = "";
        headers1.attributes = new JsonObject();
        headers1.requestId = UUID.randomUUID().toString();
        String json1 = GsonUtility.getGson().toJson(headers1);
        byte[] tmp1 = json1.getBytes(StandardCharsets.UTF_8);
        System.out.println(new String(tmp1).length());
    }
}