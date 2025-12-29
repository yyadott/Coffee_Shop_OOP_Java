package KasirApp;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.net.URL;
import java.util.List;
import java.util.Map;

public class OdooService {

    private static XmlRpcClient client;

    private static XmlRpcClient getClient() throws Exception {
        if (client == null) {
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(
                new URL(User.URL_ODOO + "/xmlrpc/2/object")
            );

            client = new XmlRpcClient();
            client.setConfig(config);
        }
        return client;
    }

    public static Object execute(
            String model,
            String method,
            List<?> args,
            Map<String, ?> kwargs
    ) throws Exception {

        if (!User.isLoggedIn()) {
            throw new Exception("User belum login ke Odoo");
        }

        return getClient().execute(
            "execute_kw",
            new Object[]{
                User.DB,
                User.uid,
                User.PASSWORD,
                model,
                method,
                args,
                kwargs
            }
        );
    }

    // OPTIONAL: cek koneksi
    public static boolean isOdooOnline() {
        try {
            execute(
                "res.users",
                "search",
                List.of(List.of()),
                Map.of("limit", 1)
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
