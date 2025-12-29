package KasirApp;

import KasirApp.TambahPesanan.Order;
import KasirApp.TambahPesanan.OrderItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

public class ProsesCheckout extends JFrame {

    private final Order currentOrder;
    private final TambahPesanan mainFrame;
    private final NumberFormat currencyFormatter =
            NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    // UI Components
    private JLabel totalLabel;
    private JComboBox<String> paymentMethodComboBox;
    private JTextField amountPaidField;
    private JTextArea receiptArea;
    private JPanel cashPanel;
    private JButton processButton;
    private JButton printPDFButton;

    // ================= PAYMENT =================
    static class Payment {
        private String paymentMethod;
        private double amountPaid;
        private final double totalAmount;
        private double change;

        public Payment(double totalAmount) {
            this.totalAmount = totalAmount;
            this.change = 0;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        public boolean calculateChange(String amountText) throws NumberFormatException {
            if (!this.paymentMethod.equals("cash")) return true;

            this.amountPaid = Double.parseDouble(amountText);
            if (this.amountPaid < this.totalAmount) {
                JOptionPane.showMessageDialog(null, "Jumlah yang dibayar tidak cukup!");
                return false;
            }
            this.change = this.amountPaid - this.totalAmount;
            return true;
        }

        public void printReceipt(JTextArea receiptArea, NumberFormat formatter, Order order) {
            receiptArea.setText("");
            receiptArea.append("====================================\n");
            receiptArea.append("         STRUK PEMBAYARAN          \n");
            receiptArea.append("====================================\n");

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
            String currentDateTime = LocalDateTime.now().format(dtf);
            receiptArea.append("Tanggal & Waktu: " + currentDateTime + "\n");
            receiptArea.append("------------------------------------\n");

            for (OrderItem item : order.getItems()) {
                receiptArea.append(String.format(
                        "%-20s %3d x %s\n",
                        item.getProduct().name(),
                        item.getQuantity(),
                        formatter.format(item.getProduct().price())
                ));
            }

            receiptArea.append("------------------------------------\n");
            receiptArea.append(String.format("%-20s %s\n", "Total Pembayaran:", formatter.format(this.totalAmount)));

            if (this.paymentMethod.equals("cash")) {
                receiptArea.append(String.format("%-20s %s\n", "Jumlah yang Dibayar:", formatter.format(this.amountPaid)));
                receiptArea.append(String.format("%-20s %s\n", "Kembalian:", formatter.format(this.change)));
            }

            receiptArea.append("Metode Pembayaran: " + this.paymentMethod.toUpperCase() + "\n");
            receiptArea.append("====================================\n");
        }

        public String getSuccessMessage(NumberFormat formatter) {
            if (this.paymentMethod.equals("cash")) {
                return "Pembayaran Berhasil! Kembalian Anda: " + formatter.format(this.change);
            } else {
                return "Pembayaran dengan " + this.paymentMethod.toUpperCase() + " Berhasil!";
            }
        }
    }

    // ================= CONSTRUCTOR =================
    public ProsesCheckout(Order currentOrder, TambahPesanan mainFrame) {
        this.currentOrder = currentOrder;
        this.mainFrame = mainFrame;
        initUI();
    }

    // ================= UI =================
    private void initUI() {
        setTitle("Proses Checkout");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        setMinimumSize(new Dimension(500, 550));

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(250, 250, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Total (Row 0)
        totalLabel = new JLabel("Total: " + currencyFormatter.format(currentOrder.calculateTotal()), JLabel.CENTER);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 24));
        totalLabel.setForeground(new Color(46, 204, 113));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(totalLabel, gbc);

        // Metode Pembayaran (Row 1)
        JLabel methodLabel = new JLabel("Pilih Metode Pembayaran:");
        methodLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.0; gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(methodLabel, gbc);

        String[] paymentMethods = {"cash", "card", "ewallet", "transfer"};
        paymentMethodComboBox = new JComboBox<>(paymentMethods);
        paymentMethodComboBox.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(paymentMethodComboBox, gbc);

        // Cash panel (Row 2)
        cashPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cashPanel.setOpaque(false);
        JLabel amountLabel = new JLabel("Jumlah Uang Diberikan (Rp): ");
        amountPaidField = new JTextField(10);
        cashPanel.add(amountLabel);
        cashPanel.add(amountPaidField);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weightx = 1.0;
        mainPanel.add(cashPanel, gbc);
        cashPanel.setVisible(paymentMethodComboBox.getSelectedItem().equals("cash"));

        // Process button (Row 3)
        processButton = new JButton("Bayar Sekarang");
        processButton.setFont(new Font("Arial", Font.BOLD, 16));
        processButton.setBackground(new Color(46, 204, 113));
        processButton.setForeground(Color.WHITE);
        processButton.setFocusPainted(false);
        processButton.setPreferredSize(new Dimension(200, 40));
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.weightx = 1.0;
        mainPanel.add(processButton, gbc);

        // Print PDF button (Row 4)
        printPDFButton = new JButton("Cetak Struk PDF");
        printPDFButton.setFont(new Font("Arial", Font.BOLD, 16));
        printPDFButton.setBackground(new Color(52, 152, 219));
        printPDFButton.setForeground(Color.WHITE);
        printPDFButton.setFocusPainted(false);
        printPDFButton.setPreferredSize(new Dimension(200, 40));
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.weightx = 1.0;
        mainPanel.add(printPDFButton, gbc);

        // Receipt area (Row 5)
        receiptArea = new JTextArea(10, 30);
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        receiptArea.setEditable(false);
        receiptArea.setBackground(new Color(240, 240, 240));
        receiptArea.setBorder(BorderFactory.createTitledBorder("Struk Transaksi"));

        JScrollPane scrollPane = new JScrollPane(receiptArea);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(scrollPane, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // Listeners
        paymentMethodComboBox.addActionListener(e -> {
            boolean isCash = paymentMethodComboBox.getSelectedItem().equals("cash");
            cashPanel.setVisible(isCash);
            pack();
        });

        processButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handlePayment();
            }
        });

        printPDFButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                printReceiptToPDF();
            }
        });

        pack();
    }

    // ================= PRINT PDF =================
    private void printReceiptToPDF() {
        String receiptText = receiptArea.getText().trim();
        if (receiptText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Struk masih kosong. Silakan lakukan pembayaran dulu.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String filePath = "struk_pembayaran.pdf";

        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();
            document.add(new Paragraph(receiptText));
            document.close();

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(new File(filePath));
            }

            JOptionPane.showMessageDialog(this, "Struk berhasil dicetak dan dibuka!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat mencetak struk.\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // ================= LOGIC PEMBAYARAN + ODOO ONLINE/OFFLINE =================
    private void handlePayment() {
        String paymentMethod = (String) paymentMethodComboBox.getSelectedItem();
        double totalAmount = currentOrder.calculateTotal();

        Payment payment = new Payment(totalAmount);
        payment.setPaymentMethod(paymentMethod);

        try {
            // Validasi cash
            if (paymentMethod.equals("cash")) {
                if (amountPaidField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Masukkan jumlah uang yang diberikan!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!payment.calculateChange(amountPaidField.getText().trim())) {
                    return;
                }
            }

            // 1) Cetak struk ke UI
            payment.printReceipt(receiptArea, currencyFormatter, currentOrder);

            // 2) Sync ke Odoo atau simpan offline (fitur dari kode kedua)
            if (!OdooService.isOdooOnline()) {
                simpanOffline();
                JOptionPane.showMessageDialog(
                        this,
                        "Odoo OFFLINE. Transaksi disimpan lokal.",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE
                );
            } else {
                try {
                    PosOrderService.createPosOrder(
                            currentOrder.calculateTotal(),
                            currentOrder.getItems()
                    );
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(
                            this,
                            "Error Odoo:\n" + ex.getMessage() + "\n\nTransaksi akan disimpan lokal (offline).",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE
                    );
                    simpanOffline();
                }
            }

            // 3) Pesan sukses pembayaran
            String successMessage = payment.getSuccessMessage(currencyFormatter);
            JOptionPane.showMessageDialog(this, successMessage, "Pembayaran Berhasil", JOptionPane.INFORMATION_MESSAGE);

            // 4) Mode selesai (jendela tetap terbuka)
            transitionToCompletionState();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Masukkan jumlah yang valid (angka)!", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error pembayaran!\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================= OFFLINE SAVE =================
    private void simpanOffline() {
        // Disimpan sebagai JSONL sederhana per transaksi (1 baris 1 transaksi)
        // Kalau kamu sudah punya currentOrder.toJson(), ganti saja payload-nya.
        String payload = buildOfflinePayload();

        try (FileWriter fw = new FileWriter("offline_order.json", true)) {
            fw.write(payload);
            fw.write("\n");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal menyimpan transaksi offline.\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String buildOfflinePayload() {
        // JSON sederhana (aman untuk log dasar). Bisa kamu upgrade sesuai kebutuhan.
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"timestamp\":\"").append(LocalDateTime.now()).append("\",");
        sb.append("\"total\":").append(currentOrder.calculateTotal()).append(",");
        sb.append("\"items\":[");
        boolean first = true;
        for (OrderItem item : currentOrder.getItems()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("{");
            sb.append("\"name\":\"").append(escapeJson(item.getProduct().name())).append("\",");
            sb.append("\"qty\":").append(item.getQuantity()).append(",");
            sb.append("\"price\":").append(item.getProduct().price()).append(",");
            sb.append("\"subtotal\":").append(item.getSubtotal());
            sb.append("}");
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // ================= FINISH STATE =================
    private void transitionToCompletionState() {
        paymentMethodComboBox.setEnabled(false);
        amountPaidField.setEnabled(false);

        processButton.setText("Selesai / Tutup");
        processButton.setBackground(new Color(52, 152, 219));

        for (ActionListener al : processButton.getActionListeners()) {
            processButton.removeActionListener(al);
        }

        processButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentOrder.clear();
                mainFrame.updateCartDisplay();
                dispose();
            }
        });
    }

    // ================= OPEN PAGE =================
    public static void showCheckoutPage(Order currentOrder, TambahPesanan mainFrame) {
        SwingUtilities.invokeLater(() -> new ProsesCheckout(currentOrder, mainFrame).setVisible(true));
    }
}
