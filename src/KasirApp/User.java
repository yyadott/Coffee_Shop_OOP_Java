package KasirApp;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import java.net.URL;
import java.util.Collections;

public class User {

    public static int uid = -1;
    public static String PASSWORD;
    public static String USERNAME;

    public static final String URL_ODOO = "https://coffe-shop3.odoo.com";
    public static final String DB = "coffe-shop3";

    public boolean login(String email, String password) {
        try {
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(new URL(URL_ODOO + "/xmlrpc/2/common"));

            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(config);

            Object uidObj = client.execute(
                "authenticate",
                new Object[]{
                    DB,
                    email,
                    password,
                    Collections.emptyMap()
                }
            );

            if (uidObj instanceof Integer && (int) uidObj > 0) {
                uid = (int) uidObj;
                USERNAME = email;
                PASSWORD = password;
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isLoggedIn() {
        return uid > 0 && PASSWORD != null;
    }
}