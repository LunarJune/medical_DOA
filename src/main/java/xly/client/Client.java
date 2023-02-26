package xly.client;

import com.google.gson.JsonObject;
import xly.doip.*;
import xly.doip.client.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;

public class Client {
    public static void main(String[] args) throws IOException, DoipException {
        DoipClient client = new DoipClient();
        AuthenticationInfo authInfo = new PasswordAuthenticationInfo("admin", "password");

        DigitalObject dobj = new DigitalObject();
        dobj.id = "35.TEST/ABC";
        dobj.type = "Document";
        JsonObject content = new JsonObject();
        content.addProperty("name", "example");
        dobj.setAttribute("content", content);

        Element el = new Element();
        el.id = "file";
        el.in = Files.newInputStream(Paths.get("DO-IRP.pdf"));
        dobj.elements = new ArrayList<>();
        dobj.elements.add(el);

        ServiceInfo serviceInfo = new ServiceInfo("35.TEST/DOIPServer", "127.0.0.1", 8888);

        DigitalObject result = client.create(dobj, authInfo, serviceInfo);
//        DigitalObject result = client.hello("", authInfo, serviceInfo);
        client.close();
    }
}
