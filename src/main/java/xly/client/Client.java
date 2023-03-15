package xly.client;

import com.google.gson.JsonObject;
import xly.Util;
import xly.doip.*;
import xly.doip.client.*;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException, DoipException, InterruptedException {
        DoipClient client = new DoipClient();
        AuthenticationInfo authInfo = new TokenAuthenticationInfo("clientid1", "token");

        ServiceInfo serviceInfo = new ServiceInfo("35.TEST/DOIPServer", "127.0.0.1", 8888);
        Scanner scan = new Scanner(System.in);
        while (true) {
            String str;
            str = scan.nextLine();
            DigitalObject dobj = new DigitalObject();
            dobj.id = "35.TEST/ABC";
            dobj.type = "Document";
            JsonObject content = new JsonObject();
            content.addProperty("name", "example");
            dobj.setAttribute("content", content);
            Element el = new Element();
            el.id = "file";
            el.in = new ByteArrayInputStream(str.getBytes());
            dobj.elements = new ArrayList<>();
            dobj.elements.add(el);
            client.create(dobj, authInfo, serviceInfo);
        }

//        DigitalObject result = client.create(dobj, authInfo, serviceInfo);
//        for(Element result_el: result.elements){
//            System.out.println("id="+result_el.id);
//            InputStream result_el_in = result_el.in;
////            FileOutputStream fos = new FileOutputStream("doip_test_client.pdf");
////            byte[] b = new byte[1024];
////            while ((result_el_in.read(b)) != -1) {
////                fos.write(b);// 写入数据
////            }
//            Util.printStream(result_el_in);
//            result_el_in.close();
////            fos.close();// 保存数据
//        }

//        DigitalObject result = client.hello("", authInfo, serviceInfo);
//        client.close();
    }
}
