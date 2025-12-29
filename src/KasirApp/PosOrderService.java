package KasirApp;

import java.util.*;

public class PosOrderService {

    public static int createPosOrder(
            double total,
            List<TambahPesanan.OrderItem> items
    ) throws Exception {

        int sessionId = PosSessionService.getOpenedSessionId();

        List<Object> lines = new ArrayList<>();

        for (TambahPesanan.OrderItem item : items) {

            // Menghitung price_subtotal = qty * price_unit
            double priceSubtotal = item.getQuantity() * item.getProduct().price();

            // Menghitung price_subtotal_incl (harga dengan pajak, misalnya 10%)
            double priceSubtotalIncl = priceSubtotal * 1.1; // Anggap pajak 10%

            Map<String, Object> lineData = new HashMap<>();
            lineData.put("product_id", Integer.parseInt(item.getProduct().code()));
            lineData.put("qty", item.getQuantity());
            lineData.put("price_unit", item.getProduct().price());
            lineData.put("price_subtotal", priceSubtotal); // Menambahkan price_subtotal
            lineData.put("price_subtotal_incl", priceSubtotalIncl); // Menambahkan price_subtotal_incl

            // Format (0, 0, {vals}) untuk one2many
            lines.add(List.of(0, 0, lineData));
        }

        // Set amount_return ke 0 karena ini transaksi tanpa pengembalian uang
        double amountReturn = 0.0;
        // Set pajak (misalnya 0, jika belum ada pajak yang dihitung)
        double amountTax = 0.0; 

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("session_id", sessionId);
        orderData.put("lines", lines);
        orderData.put("amount_total", total);
        orderData.put("amount_paid", total);
        orderData.put("amount_tax", amountTax);  // Pajak 0
        orderData.put("amount_return", amountReturn);  // Tidak ada pengembalian uang

        Object result = OdooService.execute(
            "pos.order",
            "create",
            List.of(orderData),
            Map.of()
        );

        return ((Number) result).intValue();
    }
}