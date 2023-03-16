package xly;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.*;
import xly.doip.*;
import xly.doip.client.*;
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
//        conn1.
        pool.release(conn1);
        DoipConnection conn2 = pool.get();
        System.out.println(conn1.getSocket().getLocalPort());
        System.out.println(conn2.getSocket().getLocalPort());
    }
}