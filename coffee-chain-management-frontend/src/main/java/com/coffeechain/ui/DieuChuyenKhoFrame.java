package com.coffeechain.ui;

import com.coffeechain.service.InventoryApiClient;
import com.coffeechain.service.InventoryApiClient.CreateTransferReceiptItemRequest;
import com.coffeechain.service.InventoryApiClient.CreateTransferReceiptRequest;
import com.coffeechain.service.InventoryApiClient.InventoryLotDto;
import com.coffeechain.service.InventoryApiClient.InventoryStockDto;
import com.coffeechain.service.InventoryApiClient.InventoryTransferLookupDto;
import com.coffeechain.service.InventoryApiClient.OptionDto;
import com.coffeechain.service.InventoryApiClient.TransferLotSelectionRequest;
import com.coffeechain.service.InventoryApiClient.TransferReceiptDto;
import com.coffeechain.ui.common.IconLoader;
import com.coffeechain.ui.common.RoundedButton;
import com.coffeechain.ui.common.RoundedPanel;
import com.coffeechain.ui.common.UiTheme;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DieuChuyenKhoFrame extends JFrame {
    private static final int ROOT_W = 1440;
    private static final int ROOT_H = 900;
    private static final int VIEW_H = 800;
    private static final Color WHITE = Color.WHITE;
    private static final Color PRIMARY = UiTheme.PRIMARY;
    private static final Color TEXT = UiTheme.TEXT_DARK;
    private static final Color MUTED = UiTheme.TEXT_MUTED;
    private static final Color BORDER = Color.decode("#B9B9B9");
    private static final Color SOFT_BORDER = Color.decode("#E6E6E6");
    private static final Color TABLE_HEAD = Color.decode("#D9D9D9");
    private static final Color FIELD_FILL = Color.WHITE;
    private static final Color FIELD_BORDER = Color.decode("#B9B9B9");

    private final JPanel root = new JPanel(null);
    private final InventoryApiClient apiClient = new InventoryApiClient();
    private final DecimalFormat numberFormat = new DecimalFormat("#,##0.###");

    private final JComboBox<OptionDto> sourceCombo = new JComboBox<>();
    private final JComboBox<OptionDto> destinationCombo = new JComboBox<>();
    private final JComboBox<InventoryStockDto> ingredientCombo = new JComboBox<>();
    private final JCheckBox manualLotCheckBox = new JCheckBox("Chọn lô thủ công");
    private final JTextField dateField = new JTextField(LocalDate.now().toString());
    private final JTextField creatorField = new JTextField("Hiện tại");
    private final JTextField quantityField = new JTextField();
    private final JTextArea noteArea = new JTextArea();
    private final JLabel statusLabel = new JLabel("Đang tải dữ liệu...");
    private final RoundedButton saveButton = primaryButton("Lưu phiếu");

    private final DefaultTableModel ingredientModel = new DefaultTableModel(new Object[]{"Mã NL", "Nguyên liệu", "DVT", "Tồn nguồn"}, 0) { public boolean isCellEditable(int r, int c) { return false; } };
    private final JTable ingredientTable = new JTable(ingredientModel);
    private final DefaultTableModel transferModel = new DefaultTableModel(new Object[]{"Mã NL", "Nguyên liệu", "DVT", "Số lượng", "Lô"}, 0) { public boolean isCellEditable(int r, int c) { return false; } };
    private final JTable transferTable = new JTable(transferModel);
    private final DefaultTableModel lotModel = new DefaultTableModel(new Object[]{"Mã lô", "HSD", "Còn lại", "SL chuyển"}, 0) { public boolean isCellEditable(int r, int c) { return c == 3; } };
    private final JTable lotTable = new JTable(lotModel);

    private List<InventoryStockDto> sourceIngredients = new ArrayList<>();
    private List<InventoryLotDto> currentLots = new ArrayList<>();
    private final List<TransferLine> lines = new ArrayList<>();
    private boolean loading;

    public DieuChuyenKhoFrame() {
        setTitle("Phụng Lộc - Điều chuyển kho");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        root.setPreferredSize(new Dimension(ROOT_W, ROOT_H));
        root.setBackground(WHITE);
        setContentPane(createHiddenPageScrollPane(root));
        buildHeader(); buildReceiptInfo(); buildTables(); buildInputRow(); buildLotSection(); buildFooter(); bindEvents(); loadLookups();
        pack(); setLocationRelativeTo(null);
    }

    private void buildHeader() {
        JLabel title = new JLabel("ĐIỀU CHUYỂN KHO"); title.setBounds(44, 22, 520, 40); title.setForeground(PRIMARY); title.setFont(UiTheme.bold(32)); root.add(title);
        RoundedButton backButton = primaryButton("Quay lại"); backButton.setIcon(IconLoader.svg("icons/nhap-kho/left.svg", 16, 18)); backButton.setIconTextGap(8); backButton.setBounds(1220, 30, 120, 34); backButton.addActionListener(e -> { new KhoMenuFrame().setVisible(true); dispose(); }); root.add(backButton);
    }

    private void buildReceiptInfo() {
        root.add(sectionTitle("Phiếu điều chuyển", 44, 76));
        RoundedPanel card = card(44, 104, 1352, 88); root.add(card);
        addLabel(card, "Kho nguồn:", 20, 12, 100, 18); addCombo(card, sourceCombo, 20, 34, 250, 34);
        addLabel(card, "Kho đích:", 310, 12, 100, 18); addCombo(card, destinationCombo, 310, 34, 250, 34);
        addLabel(card, "Ngày chuyển:", 600, 12, 120, 18); addField(card, dateField, 600, 34, 190, 34, false);
        addLabel(card, "Người tạo:", 830, 12, 100, 18); addField(card, creatorField, 830, 34, 190, 34, false);
        manualLotCheckBox.setBounds(1060, 38, 190, 24); manualLotCheckBox.setOpaque(false); manualLotCheckBox.setFont(UiTheme.regular(13)); card.add(manualLotCheckBox);
    }

    private void buildTables() {
        root.add(sectionTitle("DANH SÁCH NGUYÊN LIỆU", 44, 216)); configureTable(ingredientTable); JScrollPane left = hiddenScrollPane(ingredientTable); left.setBounds(44, 246, 560, 190); root.add(left);
        ingredientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); ingredientTable.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) selectIngredientFromTable(); });
        root.add(sectionTitle("DÒNG ĐIỀU CHUYỂN", 650, 216)); configureTable(transferTable); JScrollPane right = hiddenScrollPane(transferTable); right.setBounds(650, 246, 746, 190); root.add(right);
    }

    private void buildInputRow() {
        root.add(sectionTitle("CHI TIẾT ĐIỀU CHUYỂN", 44, 456));
        RoundedPanel card = card(44, 486, 1352, 92); root.add(card);
        addLabel(card, "Nguyên liệu:", 20, 12, 120, 18); addCombo(card, ingredientCombo, 20, 34, 360, 32); ingredientCombo.setRenderer(new IngredientRenderer());
        addLabel(card, "Số lượng:", 420, 12, 120, 18); addField(card, quantityField, 420, 34, 160, 32, true);
        RoundedButton addButton = primaryButton("Thêm vào phiếu"); addButton.setBounds(620, 34, 150, 32); addButton.addActionListener(e -> addLine()); card.add(addButton);
        RoundedButton removeButton = secondaryButton("Xóa dòng"); removeButton.setBounds(790, 34, 100, 32); removeButton.addActionListener(e -> removeLine()); card.add(removeButton);
    }
    private void buildLotSection() {
        root.add(sectionTitle("CHỌN LÔ THỦ CÔNG", 44, 596));
        configureTable(lotTable); JScrollPane lotScroll = hiddenScrollPane(lotTable); lotScroll.setBounds(44, 626, 760, 120); root.add(lotScroll);
        JLabel hint = new JLabel("Tắt chọn lô thủ công để backend tự xuất FEFO theo hạn sử dụng gần nhất."); hint.setBounds(830, 626, 520, 24); hint.setForeground(MUTED); hint.setFont(UiTheme.regular(13)); root.add(hint);
    }

    private void buildFooter() {
        JLabel noteLabel = new JLabel("Ghi chú điều chuyển"); noteLabel.setBounds(44, 764, 220, 20); noteLabel.setFont(UiTheme.regular(12)); root.add(noteLabel);
        RoundedPanel notePanel = card(44, 788, 820, 44); root.add(notePanel); noteArea.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12)); noteArea.setFont(UiTheme.regular(13)); JScrollPane noteScroll = new JScrollPane(noteArea); noteScroll.setBounds(1, 1, 818, 42); noteScroll.setBorder(null); hideScrollBarsButKeepWheel(noteScroll); notePanel.add(noteScroll);
        RoundedButton cancelButton = secondaryButton("Hủy"); cancelButton.setBounds(1030, 794, 96, 34); cancelButton.addActionListener(e -> resetForm()); root.add(cancelButton);
        saveButton.setBounds(1160, 794, 132, 34); saveButton.addActionListener(e -> saveTransfer()); root.add(saveButton);
        statusLabel.setBounds(44, 834, 900, 20); statusLabel.setForeground(MUTED); statusLabel.setFont(UiTheme.regular(13)); root.add(statusLabel);
    }

    private void bindEvents() {
        sourceCombo.addActionListener(e -> { if (!loading) loadStockForSource(); });
        ingredientCombo.addActionListener(e -> loadLotsForSelectedIngredient());
        manualLotCheckBox.addActionListener(e -> loadLotsForSelectedIngredient());
        lotTable.getModel().addTableModelListener(e -> { if (manualLotCheckBox.isSelected()) quantityField.setText(formatInput(sumManualLots())); });
    }

    private void loadLookups() {
        loading = true; setEnabledForm(false); statusLabel.setText("Đang tải dữ liệu điều chuyển...");
        new SwingWorker<InventoryTransferLookupDto, Void>() {
            protected InventoryTransferLookupDto doInBackground() throws Exception { return apiClient.getTransferLookups(); }
            protected void done() {
                try {
                    InventoryTransferLookupDto data = get();
                    sourceCombo.setModel(new DefaultComboBoxModel<>(data.getSourceWarehouses().toArray(new OptionDto[0])));
                    destinationCombo.setModel(new DefaultComboBoxModel<>(data.getDestinationWarehouses().toArray(new OptionDto[0])));
                    loading = false; setEnabledForm(true); loadStockForSource();
                } catch (Exception ex) { loading = false; setEnabledForm(true); showError(ex); }
            }
        }.execute();
    }

    private void loadStockForSource() {
        OptionDto source = selectedOption(sourceCombo); if (source == null || source.getId() == null) return;
        loading = true; statusLabel.setText("Đang tải tồn kho nguồn...");
        new SwingWorker<List<InventoryStockDto>, Void>() {
            protected List<InventoryStockDto> doInBackground() throws Exception { return apiClient.getTransferStock(source.getId()); }
            protected void done() {
                try {
                    sourceIngredients = get(); populateIngredientTable(sourceIngredients); ingredientCombo.setModel(new DefaultComboBoxModel<>(sourceIngredients.toArray(new InventoryStockDto[0]))); loading = false; loadLotsForSelectedIngredient(); statusLabel.setText("Đã tải " + sourceIngredients.size() + " nguyên liệu còn tồn.");
                } catch (Exception ex) { loading = false; showError(ex); }
            }
        }.execute();
    }

    private void loadLotsForSelectedIngredient() {
        if (!manualLotCheckBox.isSelected()) { lotModel.setRowCount(0); return; }
        OptionDto source = selectedOption(sourceCombo); InventoryStockDto ingredient = selectedIngredient();
        if (source == null || ingredient == null) return;
        new SwingWorker<List<InventoryLotDto>, Void>() {
            protected List<InventoryLotDto> doInBackground() throws Exception { return apiClient.getTransferLots(source.getId(), ingredient.getId()); }
            protected void done() { try { currentLots = get(); populateLotTable(currentLots); } catch (Exception ex) { showError(ex); } }
        }.execute();
    }

    private void populateIngredientTable(List<InventoryStockDto> rows) {
        ingredientModel.setRowCount(0); if (rows == null) return;
        for (InventoryStockDto row : rows) ingredientModel.addRow(new Object[]{row.getId(), row.getName(), row.getDescription(), format(row.getSoLuongTon())});
    }

    private void populateLotTable(List<InventoryLotDto> rows) {
        lotModel.setRowCount(0); if (rows == null) return;
        for (InventoryLotDto lot : rows) lotModel.addRow(new Object[]{lot.getMaLoHang(), valueOrDash(lot.getHanSuDung()), format(lot.getSoLuongConLai()), ""});
    }

    private void populateTransferTable() {
        transferModel.setRowCount(0);
        for (TransferLine line : lines) transferModel.addRow(new Object[]{line.maNguyenLieu(), line.tenNguyenLieu(), line.donViTinh(), format(line.soLuong()), line.manualLots().isEmpty() ? "FEFO" : line.manualLots().size() + " lô"});
    }

    private void selectIngredientFromTable() {
        int row = ingredientTable.getSelectedRow(); if (row < 0) return;
        Long id = Long.valueOf(ingredientTable.getValueAt(row, 0).toString());
        for (int i = 0; i < ingredientCombo.getItemCount(); i++) { InventoryStockDto item = ingredientCombo.getItemAt(i); if (item != null && id.equals(item.getId())) { ingredientCombo.setSelectedIndex(i); return; } }
    }

    private void addLine() {
        InventoryStockDto ingredient = selectedIngredient(); if (ingredient == null) { warn("Vui lòng chọn nguyên liệu"); return; }
        BigDecimal qty = parsePositive(quantityField.getText()); if (qty == null) return;
        if (ingredient.getSoLuongTon() != null && qty.compareTo(ingredient.getSoLuongTon()) > 0) { warn("Số lượng điều chuyển không được vượt tồn kho nguồn"); return; }
        List<ManualLot> manualLots = manualLotCheckBox.isSelected() ? collectManualLots() : new ArrayList<>();
        if (manualLotCheckBox.isSelected() && manualLots.isEmpty()) { warn("Vui lòng nhập số lượng theo lô hoặc tắt chọn lô thủ công"); return; }
        if (manualLotCheckBox.isSelected() && sumManualLots().compareTo(qty) != 0) { warn("Tổng số lượng theo lô phải bằng số lượng điều chuyển"); return; }
        TransferLine line = new TransferLine(ingredient.getId(), ingredient.getName(), ingredient.getDescription(), qty, manualLots);
        int idx = findLine(ingredient.getId()); if (idx >= 0) lines.set(idx, line); else lines.add(line);
        populateTransferTable(); quantityField.setText(""); statusLabel.setText("Đã thêm " + lines.size() + " dòng điều chuyển.");
    }

    private void removeLine() { int row = transferTable.getSelectedRow(); if (row < 0 || row >= lines.size()) { warn("Vui lòng chọn dòng cần xóa"); return; } lines.remove(row); populateTransferTable(); }

    private void saveTransfer() {
        OptionDto source = selectedOption(sourceCombo); OptionDto dest = selectedOption(destinationCombo);
        if (source == null || dest == null) { warn("Vui lòng chọn kho nguồn và kho đích"); return; }
        if (source.getId().equals(dest.getId())) { warn("Kho đích phải khác kho nguồn"); return; }
        if (lines.isEmpty()) { warn("Phiếu điều chuyển cần ít nhất một dòng nguyên liệu"); return; }
        CreateTransferReceiptRequest request = new CreateTransferReceiptRequest(); request.setMaKhoNguon(source.getId()); request.setMaKhoDich(dest.getId()); request.setChonLoThuCong(manualLotCheckBox.isSelected()); request.setGhiChu(noteArea.getText().trim()); request.setItems(buildRequestItems());
        saveButton.setEnabled(false); statusLabel.setText("Đang lưu phiếu điều chuyển...");
        new SwingWorker<TransferReceiptDto, Void>() {
            protected TransferReceiptDto doInBackground() throws Exception { return apiClient.createTransferReceipt(request); }
            protected void done() { saveButton.setEnabled(true); try { TransferReceiptDto res = get(); JOptionPane.showMessageDialog(DieuChuyenKhoFrame.this, "Đã tạo phiếu điều chuyển #" + res.getMaPhieuDieuChuyen() + "\n" + res.getTenKhoNguon() + " -> " + res.getTenKhoDich(), "Điều chuyển kho", JOptionPane.INFORMATION_MESSAGE); resetForm(); loadStockForSource(); } catch (Exception ex) { showError(ex); } }
        }.execute();
    }
    private List<CreateTransferReceiptItemRequest> buildRequestItems() {
        List<CreateTransferReceiptItemRequest> items = new ArrayList<>();
        for (TransferLine line : lines) {
            CreateTransferReceiptItemRequest item = new CreateTransferReceiptItemRequest();
            item.setMaNguyenLieu(line.maNguyenLieu()); item.setSoLuongDieuChuyen(line.soLuong());
            List<TransferLotSelectionRequest> lots = new ArrayList<>();
            for (ManualLot lot : line.manualLots()) { TransferLotSelectionRequest req = new TransferLotSelectionRequest(); req.setMaLoHang(lot.maLoHang()); req.setSoLuongDieuChuyen(lot.soLuong()); lots.add(req); }
            item.setLoHangDieuChuyen(lots); items.add(item);
        }
        return items;
    }

    private List<ManualLot> collectManualLots() {
        List<ManualLot> lots = new ArrayList<>();
        for (int i = 0; i < lotModel.getRowCount(); i++) {
            Object raw = lotModel.getValueAt(i, 3); if (raw == null || raw.toString().isBlank()) continue;
            BigDecimal qty = parsePositive(raw.toString()); if (qty == null) return new ArrayList<>();
            Long lotId = Long.valueOf(lotModel.getValueAt(i, 0).toString()); lots.add(new ManualLot(lotId, qty));
        }
        return lots;
    }
    private BigDecimal sumManualLots() { BigDecimal total = BigDecimal.ZERO; for (ManualLot lot : collectManualLots()) total = total.add(lot.soLuong()); return total; }
    private int findLine(Long ingredientId) { for (int i = 0; i < lines.size(); i++) if (ingredientId.equals(lines.get(i).maNguyenLieu())) return i; return -1; }
    private InventoryStockDto selectedIngredient() { return (InventoryStockDto) ingredientCombo.getSelectedItem(); }
    private OptionDto selectedOption(JComboBox<OptionDto> combo) { return (OptionDto) combo.getSelectedItem(); }
    private void resetForm() { lines.clear(); populateTransferTable(); noteArea.setText(""); quantityField.setText(""); lotModel.setRowCount(0); }
    private void setEnabledForm(boolean enabled) { sourceCombo.setEnabled(enabled); destinationCombo.setEnabled(enabled); ingredientCombo.setEnabled(enabled); quantityField.setEnabled(enabled); saveButton.setEnabled(enabled); }

    private BigDecimal parsePositive(String raw) { try { if (raw == null || raw.isBlank()) { warn("Số lượng phải lớn hơn 0"); return null; } BigDecimal value = new BigDecimal(raw.trim().replace(",", "")); if (value.compareTo(BigDecimal.ZERO) <= 0) { warn("Số lượng phải lớn hơn 0"); return null; } return value; } catch (NumberFormatException ex) { warn("Số lượng không hợp lệ"); return null; } }
    private String format(BigDecimal value) { return value == null ? "0" : numberFormat.format(value); }
    private String formatInput(BigDecimal value) { return value == null ? "" : value.stripTrailingZeros().toPlainString(); }
    private String valueOrDash(String value) { return value == null || value.isBlank() ? "-" : value; }
    private void warn(String message) { JOptionPane.showMessageDialog(this, message, "Điều chuyển kho", JOptionPane.WARNING_MESSAGE); }
    private void showError(Exception ex) { JOptionPane.showMessageDialog(this, unwrapMessage(ex), "Lỗi", JOptionPane.ERROR_MESSAGE); statusLabel.setText("Không xử lý được yêu cầu"); }
    private String unwrapMessage(Exception ex) { Throwable current = ex; while (current.getCause() != null) current = current.getCause(); return current.getMessage() == null ? "Không xử lý được yêu cầu" : current.getMessage(); }

    private JLabel sectionTitle(String text, int x, int y) { JLabel label = new JLabel(text); label.setBounds(x, y, 360, 24); label.setForeground(TEXT); label.setFont(UiTheme.bold(14)); return label; }
    private RoundedPanel card(int x, int y, int w, int h) { RoundedPanel p = new RoundedPanel(12, WHITE, SOFT_BORDER); p.setLayout(null); p.setBounds(x, y, w, h); return p; }
    private void addLabel(JPanel parent, String text, int x, int y, int w, int h) { JLabel l = new JLabel(text); l.setBounds(x, y, w, h); l.setForeground(MUTED); l.setFont(UiTheme.regular(13)); parent.add(l); }
    private void addCombo(JPanel parent, JComboBox<?> combo, int x, int y, int w, int h) {
        styleCombo(combo);
        OutlinedInputPanel panel = new OutlinedInputPanel();
        panel.setLayout(new BorderLayout());
        panel.setBounds(x, y, w, h);
        panel.add(combo, BorderLayout.CENTER);
        parent.add(panel);
    }
    private void addField(JPanel parent, JTextField field, int x, int y, int w, int h, boolean editable) {
        styleField(field);
        field.setEditable(editable);
        field.setFocusable(editable);
        OutlinedInputPanel panel = new OutlinedInputPanel();
        panel.setLayout(new BorderLayout());
        panel.setBounds(x, y, w, h);
        panel.add(field, BorderLayout.CENTER);
        parent.add(panel);
    }
    private void configureTable(JTable table) { table.setRowHeight(34); table.setFont(UiTheme.regular(13)); table.getTableHeader().setFont(UiTheme.bold(13)); table.getTableHeader().setBackground(TABLE_HEAD); table.getTableHeader().setForeground(TEXT); table.setSelectionBackground(Color.decode("#F8DCC6")); table.setGridColor(BORDER); table.setShowGrid(true); table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() { public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) { Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10)); return c; } }); }
    private JScrollPane hiddenScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(SOFT_BORDER));
        hideScrollBarsButKeepWheel(scrollPane);
        return scrollPane;
    }

    private void hideScrollBarsButKeepWheel(JScrollPane scrollPane) {
        scrollPane.setWheelScrollingEnabled(true);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 0));
        scrollPane.getVerticalScrollBar().setUnitIncrement(34);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(34);
    }

    private JScrollPane createHiddenPageScrollPane(JPanel panel) {
        JScrollPane pane = new JScrollPane(panel);
        pane.setBorder(null);
        pane.setPreferredSize(new Dimension(ROOT_W, VIEW_H));
        pane.setWheelScrollingEnabled(true);
        pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        pane.getVerticalScrollBar().setMinimumSize(new Dimension(0, 0));
        pane.getVerticalScrollBar().setMaximumSize(new Dimension(0, Integer.MAX_VALUE));
        pane.getVerticalScrollBar().setUnitIncrement(28);
        return pane;
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setUI(new DesignComboBoxUI());
        combo.setRenderer(new DesignComboBoxRenderer());
        combo.setFont(UiTheme.regular(14));
        combo.setBackground(WHITE);
        combo.setForeground(TEXT);
        combo.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 8));
        combo.setOpaque(false);
        combo.setFocusable(false);
        combo.setMaximumRowCount(8);
    }

    private void styleField(JTextField field) {
        field.setFont(UiTheme.regular(14));
        field.setForeground(TEXT);
        field.setBackground(FIELD_FILL);
        field.setDisabledTextColor(MUTED);
        field.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        field.setOpaque(false);
    }
    private static RoundedButton primaryButton(String text) { return new RoundedButton(text).background(PRIMARY).hover(UiTheme.PRIMARY_DARK).radius(10); }
    private static RoundedButton secondaryButton(String text) { RoundedButton b = new RoundedButton(text).background(Color.decode("#B9B9B9")).hover(Color.decode("#A8A8A8")).radius(10); b.setForeground(TEXT); return b; }

    private class IngredientRenderer extends DefaultListCellRenderer { public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) { JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focus); if (value instanceof InventoryStockDto i) l.setText(i.getName() + " - còn " + format(i.getSoLuongTon()) + " " + valueOrDash(i.getDescription())); return l; } }
    private static class DesignComboBoxUI extends BasicComboBoxUI {
        @Override
        public void installUI(JComponent c) {
            super.installUI(c);
            c.setOpaque(false);
        }

        @Override
        protected JButton createArrowButton() {
            JButton button = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.setColor(TEXT);
                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2 + 1;
                    g2.drawLine(cx - 5, cy - 3, cx, cy + 2);
                    g2.drawLine(cx, cy + 2, cx + 5, cy - 3);
                    g2.dispose();
                }
            };
            button.setBorder(BorderFactory.createEmptyBorder());
            button.setContentAreaFilled(false);
            button.setFocusPainted(false);
            button.setOpaque(false);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return button;
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
        }
    }

    private static class DesignComboBoxRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focus);
            if (value instanceof OptionDto option) {
                String name = option.getName() == null ? "" : option.getName();
                String desc = option.getDescription();
                label.setText(desc == null || desc.isBlank() ? name : name + " - " + desc);
            }
            label.setFont(UiTheme.regular(14));
            label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 28));
            label.setForeground(TEXT);
            label.setBackground(selected && index >= 0 ? Color.decode("#F8DCC6") : WHITE);
            return label;
        }
    }

    private static class OutlinedInputPanel extends JPanel {
        OutlinedInputPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(FIELD_FILL);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        protected void paintChildren(Graphics g) {
            super.paintChildren(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(FIELD_BORDER);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2.dispose();
        }
    }
    private record TransferLine(Long maNguyenLieu, String tenNguyenLieu, String donViTinh, BigDecimal soLuong, List<ManualLot> manualLots) {}
    private record ManualLot(Long maLoHang, BigDecimal soLuong) {}
}