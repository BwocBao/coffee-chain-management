package com.coffeechain.ui;

import com.coffeechain.service.UnitApiClient;
import com.coffeechain.service.UnitApiClient.UnitDto;
import com.coffeechain.service.UnitApiClient.UnitRequest;
import com.coffeechain.ui.common.IconLoader;
import com.coffeechain.ui.common.PermissionUtil;
import com.coffeechain.ui.common.RoundedButton;
import com.coffeechain.ui.common.UiTheme;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
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
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình quản lý đơn vị tính.
 */
public class QuanLyDonViTinhFrame extends JFrame {
    private static final int ROOT_W = 1440;
    private static final int ROOT_H = 780;
    private static final Color WHITE = Color.WHITE;
    private static final Color PRIMARY = UiTheme.PRIMARY;
    private static final Color PRIMARY_DARK = UiTheme.PRIMARY_DARK;
    private static final Color TEXT = UiTheme.TEXT_DARK;
    private static final Color MUTED = UiTheme.TEXT_MUTED;
    private static final Color BORDER = Color.decode("#B9B9B9");
    private static final Color SOFT_BORDER = Color.decode("#E6E6E6");
    private static final Color TABLE_HEAD = Color.decode("#F4E8DA");
    private static final Color DANGER = Color.decode("#BE3C2D");
    private static final Color SUCCESS = Color.decode("#3C8C5A");

    private final UnitApiClient apiClient = new UnitApiClient();
    private final JPanel root = new JPanel(null);
    private final JTextField searchField = new IconPlaceholderTextField("Tìm đơn vị tính", new FlatSVGIcon("icons/tim.svg", 18, 18));
    private final JTextField nameField = new JTextField();
    private final JTextField symbolField = new JTextField();
    private final JLabel statusLabel = new JLabel(" ");
    private final RoundedButton saveButton = primaryButton("Lưu");
    private final RoundedButton deleteButton = dangerButton("Xóa");
    private final RoundedButton clearButton = secondaryButton("Làm mới");
    private final DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Mã ĐVT", "Tên đơn vị tính", "Ký hiệu"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final List<UnitDto> units = new ArrayList<>();
    private Long selectedUnitId;
    private boolean loading;

    public QuanLyDonViTinhFrame() {
        setTitle("Phụng Lộc - Quản lý đơn vị tính");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        root.setPreferredSize(new Dimension(ROOT_W, ROOT_H));
        root.setBackground(WHITE);
        setContentPane(root);
        buildHeader();
        buildToolbar();
        buildTable();
        buildForm();
        bindEvents();
        pack();
        setLocationRelativeTo(null);
        loadUnits();
    }

    private void buildHeader() {
        JLabel title = new JLabel("QUẢN LÝ ĐƠN VỊ TÍNH");
        title.setBounds(44, 28, 560, 44);
        title.setForeground(PRIMARY);
        title.setFont(UiTheme.bold(32));
        root.add(title);

        JLabel subtitle = new JLabel("Thiết lập đơn vị đo lường cho nguyên liệu như gram, ml, chai, gói và các ký hiệu liên quan");
        subtitle.setBounds(44, 72, 820, 24);
        subtitle.setForeground(MUTED);
        subtitle.setFont(UiTheme.regular(14));
        root.add(subtitle);

        RoundedButton backButton = primaryButton("Quay lại");
        backButton.setIcon(IconLoader.svg("icons/nhap-kho/left.svg", 16, 18));
        backButton.setIconTextGap(8);
        backButton.setHorizontalAlignment(SwingConstants.CENTER);
        backButton.setBounds(1250, 34, 110, 34);
        backButton.addActionListener(e -> { new KhoMenuFrame().setVisible(true); dispose(); });
        root.add(backButton);
    }

    private void buildToolbar() {
        RoundedCard card = new RoundedCard(16, WHITE, SOFT_BORDER);
        card.setLayout(null);
        card.setBounds(44, 122, 1352, 86);
        root.add(card);
        addLabel(card, "Tìm kiếm", 24, 12, 150, 20);
        addFieldPanel(card, searchField, 24, 38, 520, 36);

        RoundedButton searchButton = primaryButton("Tìm");
        searchButton.setBounds(562, 38, 76, 36);
        searchButton.addActionListener(e -> loadUnits());
        card.add(searchButton);

        RoundedButton resetButton = secondaryButton("Reset");
        resetButton.setBounds(654, 38, 82, 36);
        resetButton.addActionListener(e -> { searchField.setText(""); loadUnits(); });
        card.add(resetButton);

        RoundedButton addButton = primaryButton("Thêm mới");
        addButton.setBounds(1210, 38, 112, 36);
        addButton.addActionListener(e -> clearForm());
        card.add(addButton);
    }

    private void buildTable() {
        JLabel title = sectionTitle("DANH SÁCH ĐƠN VỊ TÍNH");
        title.setBounds(44, 232, 360, 26);
        root.add(title);
        configureTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) fillSelectedUnit(); });
        JScrollPane scrollPane = hiddenScrollPane(table);
        scrollPane.setBounds(44, 266, 860, 340);
        root.add(scrollPane);
    }

    private void buildForm() {
        JLabel title = sectionTitle("THÔNG TIN ĐƠN VỊ TÍNH");
        title.setBounds(944, 232, 360, 26);
        root.add(title);
        RoundedCard card = new RoundedCard(16, WHITE, SOFT_BORDER);
        card.setLayout(null);
        card.setBounds(944, 266, 452, 260);
        root.add(card);

        addLabel(card, "Tên đơn vị tính", 24, 34, 160, 20);
        addFieldPanel(card, nameField, 24, 58, 404, 36);
        addLabel(card, "Ký hiệu", 24, 116, 160, 20);
        addFieldPanel(card, symbolField, 24, 140, 188, 36);

        saveButton.setBounds(24, 206, 96, 34);
        saveButton.addActionListener(e -> saveUnit());
        card.add(saveButton);
        deleteButton.setBounds(136, 206, 96, 34);
        deleteButton.addActionListener(e -> deleteUnit());
        card.add(deleteButton);
        clearButton.setBounds(248, 206, 110, 34);
        clearButton.addActionListener(e -> clearForm());
        card.add(clearButton);

        statusLabel.setBounds(44, 628, 860, 24);
        statusLabel.setForeground(MUTED);
        statusLabel.setFont(UiTheme.regular(13));
        root.add(statusLabel);
    }

    private void bindEvents() {
        searchField.addActionListener(e -> loadUnits());
    }

    private void loadUnits() {
        if (loading) return;
        loading = true;
        setButtonsEnabled(false);
        showStatus("Đang tải danh sách đơn vị tính...", false);
        String keyword = searchField.getText().trim();
        new SwingWorker<List<UnitDto>, Void>() {
            @Override protected List<UnitDto> doInBackground() throws Exception { return apiClient.searchUnits(keyword); }
            @Override protected void done() {
                try {
                    units.clear();
                    units.addAll(get());
                    populateTable();
                    showStatus("Đã tải " + units.size() + " đơn vị tính", false);
                    if (selectedUnitId != null) reselectUnit(selectedUnitId);
                } catch (Exception ex) {
                    showStatus("Không tải được đơn vị tính: " + unwrapMessage(ex), true);
                    JOptionPane.showMessageDialog(QuanLyDonViTinhFrame.this, unwrapMessage(ex), "Lỗi", JOptionPane.ERROR_MESSAGE);
                } finally {
                    loading = false;
                    setButtonsEnabled(true);
                    applyPermissions();
                }
            }
        }.execute();
    }

    private void populateTable() {
        tableModel.setRowCount(0);
        for (UnitDto unit : units) tableModel.addRow(new Object[]{unit.getMaDonViTinh(), valueOrDash(unit.getTenDonViTinh()), valueOrDash(unit.getKyHieu())});
    }

    private void fillSelectedUnit() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return;
        Long id = (Long) tableModel.getValueAt(table.convertRowIndexToModel(viewRow), 0);
        UnitDto unit = findUnit(id);
        if (unit == null) return;
        selectedUnitId = unit.getMaDonViTinh();
        nameField.setText(valueOrEmpty(unit.getTenDonViTinh()));
        symbolField.setText(valueOrEmpty(unit.getKyHieu()));
        applyPermissions();
    }

    private void saveUnit() {
        UnitRequest request = buildRequest();
        if (request == null) return;
        boolean createMode = selectedUnitId == null;
        setButtonsEnabled(false);
        showStatus(createMode ? "Đang tạo đơn vị tính..." : "Đang cập nhật đơn vị tính...", false);
        new SwingWorker<UnitDto, Void>() {
            @Override protected UnitDto doInBackground() throws Exception { return createMode ? apiClient.createUnit(request) : apiClient.updateUnit(selectedUnitId, request); }
            @Override protected void done() {
                try {
                    UnitDto saved = get();
                    selectedUnitId = saved.getMaDonViTinh();
                    showStatus(createMode ? "Đã tạo đơn vị tính" : "Đã cập nhật đơn vị tính", false);
                    loadUnits();
                } catch (Exception ex) {
                    showStatus("Lưu thất bại: " + unwrapMessage(ex), true);
                    JOptionPane.showMessageDialog(QuanLyDonViTinhFrame.this, unwrapMessage(ex), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    setButtonsEnabled(true);
                    applyPermissions();
                }
            }
        }.execute();
    }

    private UnitRequest buildRequest() {
        String name = nameField.getText().trim();
        String symbol = symbolField.getText().trim();
        if (name.isBlank()) { showWarning("Vui lòng nhập tên đơn vị tính"); nameField.requestFocusInWindow(); return null; }
        if (symbol.isBlank()) { showWarning("Vui lòng nhập ký hiệu đơn vị tính"); symbolField.requestFocusInWindow(); return null; }
        UnitRequest request = new UnitRequest();
        request.setTenDonViTinh(name);
        request.setKyHieu(symbol);
        return request;
    }

    private void deleteUnit() {
        if (selectedUnitId == null) { showWarning("Vui lòng chọn đơn vị tính cần xóa"); return; }
        int confirm = JOptionPane.showConfirmDialog(this, "Chỉ xóa được đơn vị tính chưa được nguyên liệu sử dụng. Bạn muốn xóa?", "Xóa đơn vị tính", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        Long id = selectedUnitId;
        setButtonsEnabled(false);
        showStatus("Đang xóa đơn vị tính...", false);
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception { apiClient.deleteUnit(id); return null; }
            @Override protected void done() {
                try {
                    get();
                    selectedUnitId = null;
                    clearForm();
                    showStatus("Đã xóa đơn vị tính", false);
                    loadUnits();
                } catch (Exception ex) {
                    showStatus("Xóa thất bại: " + unwrapMessage(ex), true);
                    JOptionPane.showMessageDialog(QuanLyDonViTinhFrame.this, unwrapMessage(ex), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    setButtonsEnabled(true);
                    applyPermissions();
                }
            }
        }.execute();
    }

    private void clearForm() {
        selectedUnitId = null;
        table.clearSelection();
        nameField.setText("");
        symbolField.setText("");
        showStatus("Sẵn sàng nhập đơn vị tính mới", false);
        applyPermissions();
        SwingUtilities.invokeLater(() -> nameField.requestFocusInWindow());
    }

    private void applyPermissions() {
        boolean canCreate = PermissionUtil.hasAny("UNIT:CREATE");
        boolean canUpdate = PermissionUtil.hasAny("UNIT:UPDATE");
        boolean canDelete = PermissionUtil.hasAny("UNIT:DELETE");
        saveButton.setEnabled(!loading && (selectedUnitId == null ? canCreate : canUpdate));
        deleteButton.setEnabled(!loading && selectedUnitId != null && canDelete);
    }

    private void setButtonsEnabled(boolean enabled) { saveButton.setEnabled(enabled); deleteButton.setEnabled(enabled); clearButton.setEnabled(enabled); }

    private void reselectUnit(Long id) {
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            if (id.equals(tableModel.getValueAt(row, 0))) {
                int viewRow = table.convertRowIndexToView(row);
                table.setRowSelectionInterval(viewRow, viewRow);
                table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));
                return;
            }
        }
        clearForm();
    }

    private UnitDto findUnit(Long id) {
        for (UnitDto unit : units) if (id != null && id.equals(unit.getMaDonViTinh())) return unit;
        return null;
    }

    private void addFieldPanel(JPanel parent, JTextField field, int x, int y, int w, int h) {
        styleField(field);
        RoundedInputPanel panel = new RoundedInputPanel();
        panel.setLayout(new BorderLayout());
        panel.setBounds(x, y, w, h);
        panel.add(field, BorderLayout.CENTER);
        parent.add(panel);
    }

    private void configureTable(JTable t) {
        t.setRowHeight(36);
        t.setFont(UiTheme.regular(13));
        t.getTableHeader().setFont(UiTheme.bold(13));
        t.getTableHeader().setBackground(TABLE_HEAD);
        t.getTableHeader().setForeground(TEXT);
        t.getTableHeader().setPreferredSize(new Dimension(0, 36));
        t.setSelectionBackground(Color.decode("#F8DCC6"));
        t.setSelectionForeground(TEXT);
        t.setGridColor(SOFT_BORDER);
        t.setShowGrid(true);
        t.getColumnModel().getColumn(0).setPreferredWidth(80);
        t.getColumnModel().getColumn(1).setPreferredWidth(320);
        t.getColumnModel().getColumn(2).setPreferredWidth(160);
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                if (!isSelected) c.setForeground(TEXT);
                return c;
            }
        });
    }

    private JScrollPane hiddenScrollPane(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(BorderFactory.createLineBorder(SOFT_BORDER));
        sp.setWheelScrollingEnabled(true);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        sp.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 0));
        sp.getVerticalScrollBar().setUnitIncrement(34);
        return sp;
    }

    private void styleField(JTextField field) {
        field.setFont(UiTheme.regular(14));
        field.setForeground(TEXT);
        field.setBorder(BorderFactory.createEmptyBorder(0, field instanceof IconPlaceholderTextField ? 48 : 12, 0, 12));
        field.setOpaque(false);
    }

    private void addLabel(JPanel parent, String text, int x, int y, int w, int h) { JLabel label = new JLabel(text); label.setBounds(x, y, w, h); label.setForeground(MUTED); label.setFont(UiTheme.regular(13)); parent.add(label); }
    private JLabel sectionTitle(String text) { JLabel label = new JLabel(text); label.setForeground(PRIMARY_DARK); label.setFont(UiTheme.bold(16)); return label; }
    private static RoundedButton primaryButton(String text) { return new RoundedButton(text).background(PRIMARY).hover(PRIMARY_DARK).radius(10); }
    private static RoundedButton secondaryButton(String text) { RoundedButton b = new RoundedButton(text).background(Color.decode("#B9B9B9")).hover(Color.decode("#A8A8A8")).radius(10); b.setForeground(TEXT); return b; }
    private static RoundedButton dangerButton(String text) { return new RoundedButton(text).background(DANGER).hover(Color.decode("#A83427")).radius(10); }
    private void showWarning(String msg) { JOptionPane.showMessageDialog(this, msg, "Quản lý đơn vị tính", JOptionPane.WARNING_MESSAGE); }
    private void showStatus(String msg, boolean error) { statusLabel.setForeground(error ? DANGER : SUCCESS); statusLabel.setText(msg); }
    private String unwrapMessage(Exception ex) { Throwable c = ex; while (c.getCause() != null) c = c.getCause(); return c.getMessage() == null ? "Không xử lý được yêu cầu" : c.getMessage(); }
    private String valueOrDash(String v) { return v == null || v.isBlank() ? "-" : v; }
    private String valueOrEmpty(String v) { return v == null ? "" : v; }

    private static class RoundedCard extends JPanel {
        private final int radius; private final Color fill; private final Color border;
        RoundedCard(int radius, Color fill, Color border) { this.radius = radius; this.fill = fill; this.border = border; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(fill); g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius); g2.setColor(border); g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius); g2.dispose(); super.paintComponent(g); }
    }

    private static class RoundedInputPanel extends JPanel {
        RoundedInputPanel() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(WHITE); g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10); g2.setColor(BORDER); g2.setStroke(new BasicStroke(1.2f)); g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10); g2.dispose(); super.paintComponent(g); }
    }

    private static class IconPlaceholderTextField extends JTextField {
        private final String placeholder; private final Icon icon;
        IconPlaceholderTextField(String placeholder, Icon icon) { this.placeholder = placeholder; this.icon = icon; setMargin(new Insets(0, 0, 0, 12)); }
        @Override public Insets getInsets() { return new Insets(0, 48, 0, 12); }
        @Override public Insets getInsets(Insets insets) { insets.top = 0; insets.left = 48; insets.bottom = 0; insets.right = 12; return insets; }
        @Override protected void paintComponent(Graphics g) { super.paintComponent(g); Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); if (icon != null) icon.paintIcon(this, g2, 12, (getHeight() - icon.getIconHeight()) / 2); if (getText().isEmpty()) { g2.setColor(MUTED); g2.setFont(getFont()); int y = getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 2; g2.drawString(placeholder, 48, y); } g2.dispose(); }
    }
}