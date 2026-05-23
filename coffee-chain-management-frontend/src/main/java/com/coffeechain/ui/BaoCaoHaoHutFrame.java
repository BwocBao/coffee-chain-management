package com.coffeechain.ui;

import com.coffeechain.service.WastageApiClient;
import com.coffeechain.service.SessionManager;
import com.coffeechain.service.WastageApiClient.CreateWastageRequest;
import com.coffeechain.service.WastageApiClient.OptionDto;
import com.coffeechain.service.WastageApiClient.WastageDto;
import com.coffeechain.service.WastageApiClient.WastageLookupDto;
import com.coffeechain.service.WastageApiClient.WastageLotDto;
import com.coffeechain.ui.common.IconLoader;
import com.coffeechain.ui.common.RoundedButton;
import com.coffeechain.ui.common.RoundedPanel;
import com.coffeechain.ui.common.UiTheme;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BaoCaoHaoHutFrame extends JFrame {
    private static final int ROOT_W = 1440;
    private static final int ROOT_H = 780;

    private static final Color WHITE = Color.WHITE;
    private static final Color PRIMARY = UiTheme.PRIMARY;
    private static final Color PRIMARY_DARK = UiTheme.PRIMARY_DARK;
    private static final Color TEXT = UiTheme.TEXT_DARK;
    private static final Color MUTED = UiTheme.TEXT_MUTED;
    private static final Color BORDER = Color.decode("#B9B9B9");
    private static final Color SOFT_BORDER = Color.decode("#E6E6E6");
    private static final Color TABLE_HEAD = Color.decode("#D9D9D9");
    private static final Color FIELD_FILL = Color.WHITE;
    private static final Color FIELD_BORDER = Color.decode("#B9B9B9");
    private static final Color DANGER = Color.decode("#BE3C2D");

    private final JPanel root = new JPanel(null);
    private final WastageApiClient apiClient = new WastageApiClient();
    private final DecimalFormat numberFormat = new DecimalFormat("#,##0.###");

    private final JComboBox<OptionDto> warehouseCombo = new JComboBox<>();
    private final JComboBox<OptionDto> ingredientCombo = new JComboBox<>();
    private final JComboBox<OptionDto> wastageTypeCombo = new JComboBox<>();
    private final JComboBox<OptionDto> detailWastageTypeCombo = new JComboBox<>();
    private final JComboBox<WastageLotDto> lotCombo = new JComboBox<>();

    private final JTextField reportDateField = new JTextField(LocalDate.now().toString());
    private final JTextField reporterField = new JTextField(SessionManager.getCurrentUserDisplayName());
    private final JTextField quantityField = new JTextField();
    private final JTextField remainingField = new JTextField();
    private final JTextField expiryField = new JTextField();
    private final JTextArea noteArea = new JTextArea();
    private final JLabel statusLabel = new JLabel("Đang tải dữ liệu...");
    private final RoundedButton saveButton = primaryButton("Lưu báo cáo");

    private final DefaultTableModel lotTableModel = new DefaultTableModel(
            new Object[]{"Mã lô", "Nguyên liệu", "DVT", "Còn lại", "Hạn sử dụng", "Trạng thái"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable lotTable = new JTable(lotTableModel);

    private final DefaultTableModel historyTableModel = new DefaultTableModel(
            new Object[]{"Mã phiếu", "Ngày", "Kho", "Nguyên liệu", "Lô", "SL hao hụt", "Loại", "Người báo cáo", "Ghi chú"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable historyTable = new JTable(historyTableModel);

    private List<WastageLotDto> currentLots = new ArrayList<>();
    private boolean loading;
    private boolean suppressEvents;

    public BaoCaoHaoHutFrame() {
        setTitle("Phụng Lộc - Báo cáo hao hụt");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        root.setPreferredSize(new Dimension(ROOT_W, ROOT_H));
        root.setBackground(WHITE);
        setContentPane(root);

        buildHeader();
        buildReportInfo();
        buildTables();
        buildDetailCard();
        buildNoteAndFooter();
        bindEvents();
        loadLookups();

        pack();
        setLocationRelativeTo(null);
    }

    private void buildHeader() {
        JLabel title = new JLabel("BÁO CÁO HAO HỤT");
        title.setBounds(44, 22, 520, 40);
        title.setForeground(PRIMARY);
        title.setFont(UiTheme.bold(32));
        root.add(title);

        RoundedButton backButton = primaryButton("Quay lại");
        backButton.setIcon(IconLoader.svg("icons/nhap-kho/left.svg", 16, 18));
        backButton.setIconTextGap(8);
        backButton.setHorizontalAlignment(SwingConstants.CENTER);
        backButton.setBounds(1250, 30, 110, 34);
        backButton.addActionListener(e -> {
            new KhoMenuFrame().setVisible(true);
            dispose();
        });
        root.add(backButton);
    }

    private void buildReportInfo() {
        JLabel title = new JLabel("Thông tin báo cáo");
        title.setBounds(44, 76, 220, 24);
        title.setForeground(TEXT);
        title.setFont(UiTheme.bold(16));
        root.add(title);

        RoundedPanel card = new RoundedPanel(16, WHITE, SOFT_BORDER);
        card.setLayout(null);
        card.setBounds(44, 104, 1352, 90);
        root.add(card);

        addLabel(card, "Kho ghi nhận:", 20, 12, 120, 18);
        addCombo(card, warehouseCombo, 20, 34, 250, 34);

        addLabel(card, "Nguyên liệu:", 310, 12, 120, 18);
        addCombo(card, ingredientCombo, 310, 34, 250, 34);

        addLabel(card, "Lọc loại hao hụt:", 600, 12, 140, 18);
        addCombo(card, wastageTypeCombo, 600, 34, 250, 34);

        addLabel(card, "Ngày báo cáo:", 890, 12, 120, 18);
        addField(card, reportDateField, 890, 34, 180, 34, false, LocalDate.now().toString());

        addLabel(card, "Người báo cáo:", 1110, 12, 120, 18);
        addField(card, reporterField, 1110, 34, 190, 34, false, SessionManager.getCurrentUserDisplayName());
    }

    private void buildTables() {
        JLabel lotTitle = new JLabel("LÔ HÀNG CÒN TỒN");
        lotTitle.setBounds(44, 216, 320, 24);
        lotTitle.setForeground(TEXT);
        lotTitle.setFont(UiTheme.bold(14));
        root.add(lotTitle);

        configureTable(lotTable);
        lotTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lotTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillSelectedLotToForm();
            }
        });
        JScrollPane lotScroll = hiddenScrollPane(lotTable);
        lotScroll.setBounds(44, 246, 600, 190);
        root.add(lotScroll);

        JLabel historyTitle = new JLabel("PHIẾU HAO HỤT GẦN ĐÂY");
        historyTitle.setBounds(690, 216, 320, 24);
        historyTitle.setForeground(TEXT);
        historyTitle.setFont(UiTheme.bold(14));
        root.add(historyTitle);

        configureTable(historyTable);
        historyTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths = {80, 140, 180, 160, 70, 100, 120, 120, 230};
        for (int i = 0; i < widths.length; i++) {
            historyTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        JScrollPane historyScroll = hiddenScrollPane(historyTable);
        historyScroll.setBounds(690, 246, 706, 190);
        root.add(historyScroll);
    }

    private void buildDetailCard() {
        JLabel detailTitle = new JLabel("CHI TIẾT HAO HỤT");
        detailTitle.setBounds(44, 454, 320, 24);
        detailTitle.setForeground(TEXT);
        detailTitle.setFont(UiTheme.bold(14));
        root.add(detailTitle);

        RoundedPanel card = new RoundedPanel(12, WHITE, SOFT_BORDER);
        card.setLayout(null);
        card.setBounds(44, 484, 1352, 104);
        root.add(card);

        addLabel(card, "Lô hàng:", 20, 12, 120, 18);
        addCombo(card, lotCombo, 20, 34, 220, 30);
        lotCombo.setRenderer(new WastageLotComboRenderer());

        addLabel(card, "Loại hao hụt:", 280, 12, 140, 18);
        addCombo(card, detailWastageTypeCombo, 280, 34, 190, 30);

        addLabel(card, "Số lượng hao hụt:", 510, 12, 140, 18);
        addField(card, quantityField, 510, 34, 150, 30, true, "0");

        addLabel(card, "Số lượng còn lại:", 700, 12, 140, 18);
        addField(card, remainingField, 700, 34, 150, 30, false, "0");

        addLabel(card, "Hạn sử dụng:", 890, 12, 120, 18);
        addField(card, expiryField, 890, 34, 150, 30, false, "yyyy-MM-dd");

        RoundedButton reloadButton = secondaryButton("Tải lại lô");
        reloadButton.setBounds(1080, 34, 110, 30);
        reloadButton.addActionListener(e -> loadLotsAndHistory());
        card.add(reloadButton);

        saveButton.setBounds(1220, 34, 112, 30);
        saveButton.addActionListener(e -> saveWastage());
        card.add(saveButton);
    }

    private void buildNoteAndFooter() {
        JLabel noteLabel = new JLabel("Ghi chú hao hụt");
        noteLabel.setBounds(44, 606, 240, 20);
        noteLabel.setForeground(TEXT);
        noteLabel.setFont(UiTheme.regular(12));
        root.add(noteLabel);

        RoundedPanel notePanel = new RoundedPanel(12, WHITE, SOFT_BORDER);
        notePanel.setBounds(44, 630, 904, 62);
        notePanel.setLayout(null);
        root.add(notePanel);

        noteArea.setFont(UiTheme.regular(13));
        noteArea.setForeground(TEXT);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        noteArea.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        JScrollPane noteScroll = new JScrollPane(noteArea);
        noteScroll.setBounds(1, 1, 902, 60);
        noteScroll.setBorder(null);
        hideScrollBarsButKeepWheel(noteScroll);
        notePanel.add(noteScroll);

        RoundedButton cancelButton = secondaryButton("Hủy");
        cancelButton.setBounds(1180, 640, 96, 34);
        cancelButton.addActionListener(e -> resetForm());
        root.add(cancelButton);

        statusLabel.setBounds(44, 710, 900, 24);
        statusLabel.setForeground(MUTED);
        statusLabel.setFont(UiTheme.regular(13));
        root.add(statusLabel);
    }

    private void bindEvents() {
        warehouseCombo.addActionListener(e -> {
            if (!suppressEvents) {
                loadLotsAndHistory();
            }
        });
        ingredientCombo.addActionListener(e -> {
            if (!suppressEvents) {
                loadLotsAndHistory();
            }
        });
        wastageTypeCombo.addActionListener(e -> {
            if (!suppressEvents) {
                loadHistoryOnly();
            }
        });
        lotCombo.addActionListener(e -> updateLotFieldsFromCombo());
    }

    private void loadLookups() {
        setFormEnabled(false);
        loading = true;
        statusLabel.setText("Đang tải dữ liệu báo cáo hao hụt...");

        new SwingWorker<WastageLookupDto, Void>() {
            @Override
            protected WastageLookupDto doInBackground() throws Exception {
                return apiClient.getLookups();
            }

            @Override
            protected void done() {
                try {
                    WastageLookupDto lookup = get();
                    suppressEvents = true;
                    warehouseCombo.setModel(new DefaultComboBoxModel<>(lookup.getWarehouses().toArray(new OptionDto[0])));
                    ingredientCombo.setModel(new DefaultComboBoxModel<>(lookup.getIngredients().toArray(new OptionDto[0])));
                    wastageTypeCombo.setModel(new DefaultComboBoxModel<>(buildFilterWastageTypes(lookup.getWastageTypes())));
                    detailWastageTypeCombo.setModel(new DefaultComboBoxModel<>(lookup.getWastageTypes().toArray(new OptionDto[0])));
                    suppressEvents = false;
                    setFormEnabled(true);
                    loading = false;
                    loadLotsAndHistory();
                } catch (Exception ex) {
                    loading = false;
                    setFormEnabled(true);
                    statusLabel.setText("Không tải được dữ liệu hao hụt");
                    JOptionPane.showMessageDialog(BaoCaoHaoHutFrame.this, unwrapMessage(ex), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void loadLotsAndHistory() {
        if (loading) {
            return;
        }
        OptionDto warehouse = selectedOption(warehouseCombo);
        OptionDto ingredient = selectedOption(ingredientCombo);
        if (warehouse == null || ingredient == null) {
            return;
        }

        loading = true;
        statusLabel.setText("Đang tải lô còn tồn...");
        Long maKho = warehouse.getId();
        Long maNguyenLieu = ingredient.getId();
        String loaiHaoHut = selectedWastageTypeCode();

        new SwingWorker<WastageScreenData, Void>() {
            @Override
            protected WastageScreenData doInBackground() throws Exception {
                List<WastageLotDto> lots = apiClient.getAvailableLots(maKho, maNguyenLieu);
                List<WastageDto> history = apiClient.searchWastages(maKho, maNguyenLieu, loaiHaoHut, null, null, null);
                return new WastageScreenData(lots, history);
            }

            @Override
            protected void done() {
                try {
                    WastageScreenData data = get();
                    currentLots = data.lots() == null ? new ArrayList<>() : data.lots();
                    populateLotTable(currentLots);
                    lotCombo.setModel(new DefaultComboBoxModel<>(currentLots.toArray(new WastageLotDto[0])));
                    populateHistoryTable(data.history());
                    updateLotFieldsFromCombo();
                    statusLabel.setText("Đã tải " + currentLots.size() + " lô còn tồn.");
                } catch (Exception ex) {
                    statusLabel.setText("Không tải được dữ liệu hao hụt");
                    JOptionPane.showMessageDialog(BaoCaoHaoHutFrame.this, unwrapMessage(ex), "Lỗi", JOptionPane.ERROR_MESSAGE);
                } finally {
                    loading = false;
                }
            }
        }.execute();
    }

    private void loadHistoryOnly() {
        if (loading) {
            return;
        }
        OptionDto warehouse = selectedOption(warehouseCombo);
        OptionDto ingredient = selectedOption(ingredientCombo);
        if (warehouse == null || ingredient == null) {
            return;
        }

        loading = true;
        statusLabel.setText("Đang tải phiếu hao hụt...");
        Long maKho = warehouse.getId();
        Long maNguyenLieu = ingredient.getId();
        String loaiHaoHut = selectedWastageTypeCode();

        new SwingWorker<List<WastageDto>, Void>() {
            @Override
            protected List<WastageDto> doInBackground() throws Exception {
                return apiClient.searchWastages(maKho, maNguyenLieu, loaiHaoHut, null, null, null);
            }

            @Override
            protected void done() {
                try {
                    List<WastageDto> history = get();
                    populateHistoryTable(history);
                    statusLabel.setText("Đã tải " + history.size() + " phiếu hao hụt.");
                } catch (Exception ex) {
                    statusLabel.setText("Không tải được phiếu hao hụt");
                    JOptionPane.showMessageDialog(BaoCaoHaoHutFrame.this, unwrapMessage(ex), "Lỗi", JOptionPane.ERROR_MESSAGE);
                } finally {
                    loading = false;
                }
            }
        }.execute();
    }

    private void saveWastage() {
        OptionDto warehouse = selectedOption(warehouseCombo);
        OptionDto ingredient = selectedOption(ingredientCombo);
        WastageLotDto lot = (WastageLotDto) lotCombo.getSelectedItem();
        OptionDto type = selectedOption(detailWastageTypeCombo);

        if (warehouse == null || ingredient == null || type == null) {
            showWarning("Vui lòng chọn kho, nguyên liệu và loại hao hụt");
            return;
        }
        if (lot == null) {
            showWarning("Vui lòng chọn lô hàng còn tồn");
            return;
        }

        BigDecimal quantity = parsePositiveDecimal(quantityField.getText(), "Số lượng hao hụt phải lớn hơn 0");
        if (quantity == null) {
            return;
        }
        BigDecimal remaining = lot.getSoLuongConLai() == null ? BigDecimal.ZERO : lot.getSoLuongConLai();
        if (quantity.compareTo(remaining) > 0) {
            showWarning("Số lượng hao hụt không được vượt quá số lượng còn lại của lô");
            return;
        }

        CreateWastageRequest request = new CreateWastageRequest();
        request.setMaKho(warehouse.getId());
        request.setMaNguyenLieu(ingredient.getId());
        request.setMaLoHang(lot.getMaLoHang());
        request.setSoLuongHaoHut(quantity);
        request.setLoaiHaoHut(type.getCode());
        request.setGhiChu(noteArea.getText().trim());

        saveButton.setEnabled(false);
        statusLabel.setText("Đang lưu báo cáo hao hụt...");

        new SwingWorker<WastageDto, Void>() {
            @Override
            protected WastageDto doInBackground() throws Exception {
                return apiClient.createWastage(request);
            }

            @Override
            protected void done() {
                saveButton.setEnabled(true);
                try {
                    WastageDto response = get();
                    JOptionPane.showMessageDialog(
                            BaoCaoHaoHutFrame.this,
                            "Đã tạo báo cáo hao hụt #" + response.getMaPhieuHaoHut()
                                    + "\nNguyên liệu: " + response.getTenNguyenLieu()
                                    + "\nSố lượng: " + formatNumber(response.getSoLuongHaoHut()) + " " + valueOrDash(response.getDonViTinh()),
                            "Báo cáo hao hụt",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    resetForm();
                    suppressEvents = true;
                    selectFilterWastageType(response.getLoaiHaoHut());
                    suppressEvents = false;
                    loadLotsAndHistory();
                } catch (Exception ex) {
                    statusLabel.setText("Lưu báo cáo hao hụt thất bại");
                    JOptionPane.showMessageDialog(BaoCaoHaoHutFrame.this, unwrapMessage(ex), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void populateLotTable(List<WastageLotDto> lots) {
        lotTableModel.setRowCount(0);
        if (lots == null) {
            return;
        }
        for (WastageLotDto lot : lots) {
            lotTableModel.addRow(new Object[]{
                    lot.getMaLoHang(),
                    valueOrDash(lot.getTenNguyenLieu()),
                    valueOrDash(lot.getDonViTinh()),
                    formatNumber(lot.getSoLuongConLai()),
                    valueOrDash(lot.getHanSuDung()),
                    valueOrDash(lot.getTrangThai())
            });
        }
    }

    private void populateHistoryTable(List<WastageDto> rows) {
        historyTableModel.setRowCount(0);
        if (rows == null) {
            return;
        }
        for (WastageDto item : rows) {
            historyTableModel.addRow(new Object[]{
                    item.getMaPhieuHaoHut(),
                    formatDateTime(item.getNgayHaoHut()),
                    valueOrDash(item.getTenKho()),
                    valueOrDash(item.getTenNguyenLieu()),
                    item.getMaLoHang() == null ? "-" : item.getMaLoHang(),
                    formatNumber(item.getSoLuongHaoHut()) + " " + valueOrDash(item.getDonViTinh()),
                    wastageTypeLabel(item.getLoaiHaoHut()),
                    valueOrDash(item.getTenNguoiBaoCao()),
                    valueOrDash(item.getGhiChu())
            });
        }
    }

    private void fillSelectedLotToForm() {
        int row = lotTable.getSelectedRow();
        if (row < 0 || row >= lotTable.getRowCount()) {
            return;
        }
        Long lotId = Long.valueOf(lotTable.getValueAt(row, 0).toString());
        for (int i = 0; i < lotCombo.getItemCount(); i++) {
            WastageLotDto lot = lotCombo.getItemAt(i);
            if (lot != null && lotId.equals(lot.getMaLoHang())) {
                lotCombo.setSelectedIndex(i);
                quantityField.requestFocusInWindow();
                return;
            }
        }
    }

    private void updateLotFieldsFromCombo() {
        WastageLotDto lot = (WastageLotDto) lotCombo.getSelectedItem();
        if (lot == null) {
            remainingField.setText("");
            expiryField.setText("");
            return;
        }
        remainingField.setText(formatNumber(lot.getSoLuongConLai()) + " " + valueOrDash(lot.getDonViTinh()));
        expiryField.setText(valueOrDash(lot.getHanSuDung()));
    }

    private void resetForm() {
        quantityField.setText("");
        noteArea.setText("");
        if (lotCombo.getItemCount() > 0) {
            lotCombo.setSelectedIndex(0);
        }
        updateLotFieldsFromCombo();
        SwingUtilities.invokeLater(() -> quantityField.requestFocusInWindow());
    }

    private void setFormEnabled(boolean enabled) {
        warehouseCombo.setEnabled(enabled);
        ingredientCombo.setEnabled(enabled);
        wastageTypeCombo.setEnabled(enabled);
        detailWastageTypeCombo.setEnabled(enabled);
        lotCombo.setEnabled(enabled);
        quantityField.setEnabled(enabled);
        noteArea.setEnabled(enabled);
        saveButton.setEnabled(enabled);
    }

    private OptionDto[] buildFilterWastageTypes(List<OptionDto> types) {
        List<OptionDto> items = new ArrayList<>();
        OptionDto all = new OptionDto();
        all.setName("Tất cả loại");
        all.setCode(null);
        items.add(all);
        if (types != null) {
            items.addAll(types);
        }
        return items.toArray(new OptionDto[0]);
    }

    private void selectFilterWastageType(String code) {
        if (code == null) {
            return;
        }
        for (int i = 0; i < wastageTypeCombo.getItemCount(); i++) {
            OptionDto item = wastageTypeCombo.getItemAt(i);
            if (item != null && code.equals(item.getCode())) {
                wastageTypeCombo.setSelectedIndex(i);
                return;
            }
        }
    }

    private OptionDto selectedOption(JComboBox<OptionDto> combo) {
        return (OptionDto) combo.getSelectedItem();
    }

    private String selectedWastageTypeCode() {
        OptionDto type = selectedOption(wastageTypeCombo);
        return type == null ? null : type.getCode();
    }

    private BigDecimal parsePositiveDecimal(String raw, String message) {
        if (raw == null || raw.isBlank()) {
            showWarning(message);
            return null;
        }
        try {
            BigDecimal value = new BigDecimal(raw.trim().replace(",", ""));
            if (value.compareTo(BigDecimal.ZERO) <= 0) {
                showWarning(message);
                return null;
            }
            return value;
        } catch (NumberFormatException ex) {
            showWarning(message);
            return null;
        }
    }

    private void configureTable(JTable table) {
        table.setRowHeight(34);
        table.setFont(UiTheme.regular(13));
        table.getTableHeader().setFont(UiTheme.bold(13));
        table.getTableHeader().setBackground(TABLE_HEAD);
        table.getTableHeader().setForeground(TEXT);
        table.getTableHeader().setPreferredSize(new Dimension(0, 34));
        table.setSelectionBackground(Color.decode("#F8DCC6"));
        table.setSelectionForeground(TEXT);
        table.setGridColor(BORDER);
        table.setShowGrid(true);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                if (!isSelected) {
                    component.setForeground(column == 6 ? DANGER : TEXT);
                }
                return component;
            }
        });
    }

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

    private void addCombo(JPanel parent, JComboBox<?> combo, int x, int y, int w, int h) {
        styleCombo(combo);
        OutlinedInputPanel panel = new OutlinedInputPanel();
        panel.setLayout(new BorderLayout());
        panel.setBounds(x, y, w, h);
        panel.add(combo, BorderLayout.CENTER);
        parent.add(panel);
    }

    private void addField(JPanel parent, JTextField field, int x, int y, int w, int h, boolean editable, String tooltip) {
        styleField(field, tooltip);
        field.setEditable(editable);
        field.setFocusable(editable);
        OutlinedInputPanel panel = new OutlinedInputPanel();
        panel.setLayout(new BorderLayout());
        panel.setBounds(x, y, w, h);
        panel.add(field, BorderLayout.CENTER);
        parent.add(panel);
    }

    private void addLabel(JPanel parent, String text, int x, int y, int w, int h) {
        JLabel label = new JLabel(text);
        label.setBounds(x, y, w, h);
        label.setForeground(MUTED);
        label.setFont(UiTheme.regular(13));
        parent.add(label);
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

    private void styleField(JTextField field, String tooltip) {
        field.setFont(UiTheme.regular(14));
        field.setForeground(TEXT);
        field.setBackground(FIELD_FILL);
        field.setToolTipText(tooltip);
        field.setDisabledTextColor(MUTED);
        field.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        field.setOpaque(false);
    }

    private static RoundedButton primaryButton(String text) {
        return new RoundedButton(text).background(PRIMARY).hover(PRIMARY_DARK).radius(10);
    }

    private static RoundedButton secondaryButton(String text) {
        RoundedButton button = new RoundedButton(text).background(Color.decode("#B9B9B9")).hover(Color.decode("#A8A8A8")).radius(10);
        button.setForeground(TEXT);
        return button;
    }

    private String wastageTypeLabel(String code) {
        if ("EXPIRED".equalsIgnoreCase(code)) return "Hết hạn";
        if ("DAMAGED".equalsIgnoreCase(code)) return "Hư hỏng";
        if ("LOST".equalsIgnoreCase(code)) return "Thất thoát";
        if ("SPILL".equalsIgnoreCase(code)) return "Đổ vỡ";
        return valueOrDash(code);
    }

    private String formatNumber(BigDecimal value) {
        return value == null ? "0" : numberFormat.format(value);
    }

    private String formatDateTime(String value) {
        if (value == null || value.isBlank()) return "-";
        return value.length() > 19 ? value.substring(0, 19).replace('T', ' ') : value.replace('T', ' ');
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Báo cáo hao hụt", JOptionPane.WARNING_MESSAGE);
    }

    private String unwrapMessage(Exception ex) {
        Throwable current = ex;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? "Không xử lý được yêu cầu" : current.getMessage();
    }

    private record WastageScreenData(List<WastageLotDto> lots, List<WastageDto> history) {}

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
        public void paintCurrentValueBackground(Graphics g, java.awt.Rectangle bounds, boolean hasFocus) {
        }
    }

    private static class DesignComboBoxRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof OptionDto option) {
                label.setText(option.getName() == null ? "" : option.getName());
            }
            label.setFont(UiTheme.regular(14));
            label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 28));
            label.setForeground(TEXT);
            label.setBackground(isSelected && index >= 0 ? Color.decode("#F8DCC6") : WHITE);
            return label;
        }
    }

    private class WastageLotComboRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof WastageLotDto lot) {
                label.setText("Lô #" + lot.getMaLoHang() + " - còn " + formatNumber(lot.getSoLuongConLai()) + " " + valueOrDash(lot.getDonViTinh()));
            }
            label.setFont(UiTheme.regular(14));
            label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 28));
            label.setForeground(TEXT);
            label.setBackground(isSelected && index >= 0 ? Color.decode("#F8DCC6") : WHITE);
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
}
