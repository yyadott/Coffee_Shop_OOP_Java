package KasirApp;

import java.util.*;

public class SalesReportService {

    public static double getTotalSalesToday() throws Exception {

        Object result = OdooService.execute(
                "pos.order",
                "search_read",
                List.of(List.of()), // ambil semua order
                Map.of("fields", List.of("amount_total"))
        );

        double total = 0;
        Object[] orders = (Object[]) result;

        for (Object o : orders) {
            Map<String, Object> m = (Map<String, Object>) o;
            total += ((Number) m.get("amount_total")).doubleValue();
        }

        return total;
    }
}