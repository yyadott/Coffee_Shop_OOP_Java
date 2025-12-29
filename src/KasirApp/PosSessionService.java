package KasirApp;

import java.util.List;
import java.util.Map;

public class PosSessionService {

    public static int getOpenedSessionId() throws Exception {

        Object result = OdooService.execute(
            "pos.session",
            "search_read",
            List.of(List.of(
                List.of("state", "=", "opened")
            )),
            Map.of(
                "fields", List.of("id"),
                "limit", 1
            )
        );

        Object[] sessions = (Object[]) result;

        if (sessions.length == 0) {
            throw new Exception("Tidak ada POS session yang terbuka");
        }

        Map<String, Object> session = (Map<String, Object>) sessions[0];
        return ((Number) session.get("id")).intValue();
    }
}