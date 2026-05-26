package com.coffeechain.ui;

import com.coffeechain.service.BranchApiClient;
import com.coffeechain.service.BranchApiClient.BranchDto;
import com.coffeechain.service.BranchApiClient.BranchRequest;
import com.coffeechain.service.BranchApiClient.BranchStatisticsDto;
import com.coffeechain.ui.common.IconLoader;
import com.coffeechain.ui.common.PermissionUtil;
import com.coffeechain.ui.common.RoundedButton;
import com.coffeechain.ui.common.UiTheme;
import com.formdev.flatlaf.extras.FlatSVGIcon;
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
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
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
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/** Màn hình quản lý chi nhánh. */
public class QuanLyChiNhanhFrame extends JFrame {
  private static final int ROOT_W = 1440;
  private static final int ROOT_H = 820;
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
  private static final Color WARNING = Color.decode("#B7791F");

  private final BranchApiClient apiClient = new BranchApiClient();
  private final JPanel root = new JPanel(null);
  private final JTextField searchField =
      new IconPlaceholderTextField("Tìm chi nhánh", new FlatSVGIcon("icons/tim.svg", 18, 18));
  private final JComboBox<OptionDto> filterStatusCombo = new JComboBox<>();
  private final JTextField nameField = new JTextField();
  private final JTextField phoneField = new JTextField();
  private final JTextArea addressArea = new JTextArea();
  private final JComboBox<OptionDto> statusCombo = new JComboBox<>();
  private final JLabel statusLabel = new JLabel(" ");
  private final JLabel totalValue = statValue();
  private final JLabel activeValue = statValue();
  private final JLabel closedValue = statValue();
  private final JLabel maintenanceValue = statValue();
  private final RoundedButton saveButton = primaryButton("Lưu");
  private final RoundedButton clearButton = secondaryButton("Làm mới");

  private final DefaultTableModel tableModel =
      new DefaultTableModel(
          new Object[] {
            "Mã CN", "Tên chi nhánh", "Địa chỉ", "SĐT", "Kho", "Nhân viên", "Trạng thái"
          },
          0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return false;
        }
      };
  private final JTable table = new JTable(tableModel);
  private final List<BranchDto> branches = new ArrayList<>();
  private Long selectedBranchId;
  private boolean loading;

  public QuanLyChiNhanhFrame() {
    setTitle("Phụng Lộc - Quản lý chi nhánh");
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setResizable(false);
    root.setPreferredSize(new Dimension(ROOT_W, ROOT_H));
    root.setBackground(WHITE);
    setContentPane(root);
    buildHeader();
    buildStats();
    buildToolbar();
    buildTable();
    buildForm();
    bindEvents();
    pack();
    setLocationRelativeTo(null);
    populateStatusCombos();
    loadAll();
  }

  private void buildHeader() {
    JLabel title = new JLabel("QUẢN LÝ CHI NHÁNH");
    title.setBounds(44, 24, 560, 44);
    title.setForeground(PRIMARY);
    title.setFont(UiTheme.bold(32));
    root.add(title);
    JLabel subtitle =
        new JLabel(
            "Theo dõi thông tin chi nhánh, kho liên kết, số nhân viên và trạng thái hoạt động");
    subtitle.setBounds(44, 68, 780, 24);
    subtitle.setForeground(MUTED);
    subtitle.setFont(UiTheme.regular(14));
    root.add(subtitle);
    RoundedButton backButton = primaryButton("Quay lại");
    backButton.setIcon(IconLoader.svg("icons/nhap-kho/left.svg", 16, 18));
    backButton.setIconTextGap(8);
    backButton.setHorizontalAlignment(SwingConstants.CENTER);
    backButton.setBounds(1250, 30, 110, 34);
    backButton.addActionListener(
        e -> {
          new MenuTongFrame().setVisible(true);
          dispose();
        });
    root.add(backButton);
  }

  private void buildStats() {
    addStatCard("Tổng chi nhánh", totalValue, 44, 112);
    addStatCard("Đang hoạt động", activeValue, 395, 112);
    addStatCard("Đã đóng", closedValue, 746, 112);
    addStatCard("Bảo trì", maintenanceValue, 1097, 112);
  }

  private void addStatCard(String title, JLabel value, int x, int y) {
    RoundedCard card = new RoundedCard(14, WHITE, SOFT_BORDER);
    card.setLayout(null);
    card.setBounds(x, y, 296, 82);
    root.add(card);
    JLabel label = new JLabel(title);
    label.setBounds(24, 14, 180, 20);
    label.setForeground(MUTED);
    label.setFont(UiTheme.regular(13));
    card.add(label);
    value.setBounds(24, 38, 230, 30);
    card.add(value);
  }

  private void buildToolbar() {
    RoundedCard card = new RoundedCard(16, WHITE, SOFT_BORDER);
    card.setLayout(null);
    card.setBounds(44, 214, 1352, 86);
    root.add(card);
    addLabel(card, "Tìm kiếm", 24, 12, 150, 20);
    addFieldPanel(card, searchField, 24, 38, 520, 36);
    addLabel(card, "Trạng thái", 574, 12, 120, 20);
    addCombo(card, filterStatusCombo, 574, 38, 220, 36);
    RoundedButton searchButton = primaryButton("Lọc");
    searchButton.setBounds(824, 38, 72, 36);
    searchButton.addActionListener(e -> loadBranches());
    card.add(searchButton);
    RoundedButton resetButton = secondaryButton("Reset");
    resetButton.setBounds(910, 38, 82, 36);
    resetButton.addActionListener(e -> resetFilters());
    card.add(resetButton);
    RoundedButton addButton = primaryButton("Thêm mới");
    addButton.setBounds(1210, 38, 112, 36);
    addButton.addActionListener(e -> clearForm());
    card.add(addButton);
  }

  private void buildTable() {
    JLabel title = sectionTitle("DANH SÁCH CHI NHÁNH");
    title.setBounds(44, 324, 360, 26);
    root.add(title);
    configureTable(table);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table
        .getSelectionModel()
        .addListSelectionListener(
            e -> {
              if (!e.getValueIsAdjusting()) fillSelectedBranch();
            });
    JScrollPane scrollPane = hiddenScrollPane(table);
    scrollPane.setBounds(44, 358, 900, 330);
    root.add(scrollPane);
  }

  private void buildForm() {
    JLabel title = sectionTitle("THÔNG TIN CHI NHÁNH");
    title.setBounds(984, 324, 360, 26);
    root.add(title);
    RoundedCard card = new RoundedCard(16, WHITE, SOFT_BORDER);
    card.setLayout(null);
    card.setBounds(984, 358, 412, 330);
    root.add(card);
    addLabel(card, "Tên chi nhánh", 24, 24, 160, 20);
    addFieldPanel(card, nameField, 24, 48, 364, 36);
    addLabel(card, "Số điện thoại", 24, 102, 160, 20);
    addFieldPanel(card, phoneField, 24, 126, 140, 36);
    addLabel(card, "Trạng thái", 188, 102, 160, 20);
    addCombo(card, statusCombo, 188, 126, 200, 36);
    addLabel(card, "Địa chỉ", 24, 180, 160, 20);
    styleArea(addressArea);
    RoundedInputPanel addressPanel = new RoundedInputPanel();
    addressPanel.setLayout(new BorderLayout());
    addressPanel.setBounds(24, 204, 364, 62);
    JScrollPane addressScroll = new JScrollPane(addressArea);
    addressScroll.setBorder(null);
    addressScroll.setOpaque(false);
    addressScroll.getViewport().setOpaque(false);
    hideScrollBars(addressScroll);
    addressPanel.add(addressScroll, BorderLayout.CENTER);
    card.add(addressPanel);
    saveButton.setBounds(24, 284, 96, 34);
    saveButton.addActionListener(e -> saveBranch());
    card.add(saveButton);
    clearButton.setBounds(136, 284, 110, 34);
    clearButton.addActionListener(e -> clearForm());
    card.add(clearButton);
    statusLabel.setBounds(44, 708, 900, 24);
    statusLabel.setForeground(MUTED);
    statusLabel.setFont(UiTheme.regular(13));
    root.add(statusLabel);
  }

  private void bindEvents() {
    searchField.addActionListener(e -> loadBranches());
  }

  private void populateStatusCombos() {
    filterStatusCombo.removeAllItems();
    filterStatusCombo.addItem(new OptionDto(null, "Tất cả trạng thái"));
    filterStatusCombo.addItem(new OptionDto("ACTIVE", "Đang hoạt động"));
    filterStatusCombo.addItem(new OptionDto("CLOSED", "Đã đóng"));
    filterStatusCombo.addItem(new OptionDto("MAINTENANCE", "Bảo trì"));
    statusCombo.removeAllItems();
    statusCombo.addItem(new OptionDto("ACTIVE", "Đang hoạt động"));
    statusCombo.addItem(new OptionDto("CLOSED", "Đã đóng"));
    statusCombo.addItem(new OptionDto("MAINTENANCE", "Bảo trì"));
  }

  private void loadAll() {
    loadStatistics();
    loadBranches();
  }

  private void loadStatistics() {
    new SwingWorker<BranchStatisticsDto, Void>() {
      @Override
      protected BranchStatisticsDto doInBackground() throws Exception {
        return apiClient.getStatistics();
      }

      @Override
      protected void done() {
        try {
          updateStats(get());
        } catch (Exception ignored) {
          updateStats(null);
        }
      }
    }.execute();
  }

  private void updateStats(BranchStatisticsDto stats) {
    totalValue.setText(
        String.valueOf(
            stats == null || stats.getTongSoChiNhanh() == null ? 0 : stats.getTongSoChiNhanh()));
    activeValue.setText(
        String.valueOf(
            stats == null || stats.getSoChiNhanhDangHoatDong() == null
                ? 0
                : stats.getSoChiNhanhDangHoatDong()));
    closedValue.setText(
        String.valueOf(
            stats == null || stats.getSoChiNhanhDaDong() == null
                ? 0
                : stats.getSoChiNhanhDaDong()));
    maintenanceValue.setText(
        String.valueOf(
            stats == null || stats.getSoChiNhanhBaoTri() == null
                ? 0
                : stats.getSoChiNhanhBaoTri()));
  }

  private void loadBranches() {
    if (loading) return;
    loading = true;
    setButtonsEnabled(false);
    showStatus("Đang tải danh sách chi nhánh...", false);
    String keyword = searchField.getText().trim();
    String status = selectedCode(filterStatusCombo);
    new SwingWorker<List<BranchDto>, Void>() {
      @Override
      protected List<BranchDto> doInBackground() throws Exception {
        return apiClient.searchBranches(keyword, status);
      }

      @Override
      protected void done() {
        try {
          branches.clear();
          branches.addAll(get());
          populateTable();
          showStatus("Đã tải " + branches.size() + " chi nhánh", false);
          if (selectedBranchId != null) reselectBranch(selectedBranchId);
        } catch (Exception ex) {
          showStatus("Không tải được chi nhánh: " + unwrapMessage(ex), true);
          JOptionPane.showMessageDialog(
              QuanLyChiNhanhFrame.this, unwrapMessage(ex), "Lỗi", JOptionPane.ERROR_MESSAGE);
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
    for (BranchDto b : branches) {
      tableModel.addRow(
          new Object[] {
            b.getMaChiNhanh(),
            valueOrDash(b.getTenChiNhanh()),
            valueOrDash(b.getDiaChi()),
            valueOrDash(b.getSoDienThoai()),
            valueOrDash(b.getTenKho()),
            b.getSoNhanVien() == null ? 0 : b.getSoNhanVien(),
            statusText(b.getTrangThai())
          });
    }
  }

  private void fillSelectedBranch() {
    int viewRow = table.getSelectedRow();
    if (viewRow < 0) return;
    Long id = (Long) tableModel.getValueAt(table.convertRowIndexToModel(viewRow), 0);
    BranchDto b = findBranch(id);
    if (b == null) return;
    selectedBranchId = b.getMaChiNhanh();
    nameField.setText(valueOrEmpty(b.getTenChiNhanh()));
    phoneField.setText(valueOrEmpty(b.getSoDienThoai()));
    addressArea.setText(valueOrEmpty(b.getDiaChi()));
    selectStatus(statusCombo, b.getTrangThai());
    applyPermissions();
  }

  private void saveBranch() {
    BranchRequest request = buildRequest();
    if (request == null) return;
    boolean createMode = selectedBranchId == null;
    setButtonsEnabled(false);
    showStatus(createMode ? "Đang tạo chi nhánh..." : "Đang cập nhật chi nhánh...", false);
    new SwingWorker<BranchDto, Void>() {
      @Override
      protected BranchDto doInBackground() throws Exception {
        return createMode
            ? apiClient.createBranch(request)
            : apiClient.updateBranch(selectedBranchId, request);
      }

      @Override
      protected void done() {
        try {
          BranchDto saved = get();
          selectedBranchId = saved.getMaChiNhanh();
          showStatus(createMode ? "Đã tạo chi nhánh" : "Đã cập nhật chi nhánh", false);
          loadStatistics();
          loadBranches();
        } catch (Exception ex) {
          showStatus("Lưu thất bại: " + unwrapMessage(ex), true);
          JOptionPane.showMessageDialog(
              QuanLyChiNhanhFrame.this, unwrapMessage(ex), "Lỗi", JOptionPane.ERROR_MESSAGE);
          setButtonsEnabled(true);
          applyPermissions();
        }
      }
    }.execute();
  }

  private BranchRequest buildRequest() {
    String name = nameField.getText().trim();
    String phone = phoneField.getText().trim();
    String address = addressArea.getText().trim();
    if (name.isBlank()) {
      showWarning("Vui lòng nhập tên chi nhánh");
      nameField.requestFocusInWindow();
      return null;
    }
    if (address.isBlank()) {
      showWarning("Vui lòng nhập địa chỉ chi nhánh");
      addressArea.requestFocusInWindow();
      return null;
    }
    if (phone.isBlank()) {
      showWarning("Vui lòng nhập số điện thoại chi nhánh");
      phoneField.requestFocusInWindow();
      return null;
    }
    BranchRequest request = new BranchRequest();
    request.setTenChiNhanh(name);
    request.setDiaChi(address);
    request.setSoDienThoai(phone);
    request.setTrangThai(selectedBranchId == null ? null : selectedCode(statusCombo));
    return request;
  }

  private void clearForm() {
    selectedBranchId = null;
    table.clearSelection();
    nameField.setText("");
    phoneField.setText("");
    addressArea.setText("");
    if (statusCombo.getItemCount() > 0) statusCombo.setSelectedIndex(0);
    showStatus("Sẵn sàng nhập chi nhánh mới", false);
    applyPermissions();
    SwingUtilities.invokeLater(() -> nameField.requestFocusInWindow());
  }

  private void resetFilters() {
    searchField.setText("");
    if (filterStatusCombo.getItemCount() > 0) filterStatusCombo.setSelectedIndex(0);
    loadBranches();
  }

  private void applyPermissions() {
    boolean canCreate = PermissionUtil.hasAny("BRANCH:CREATE");
    boolean canUpdate = PermissionUtil.hasAny("BRANCH:UPDATE");
    saveButton.setEnabled(!loading && (selectedBranchId == null ? canCreate : canUpdate));
  }

  private void setButtonsEnabled(boolean enabled) {
    saveButton.setEnabled(enabled);
    clearButton.setEnabled(enabled);
  }

  private void reselectBranch(Long id) {
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

  private BranchDto findBranch(Long id) {
    for (BranchDto b : branches) if (id != null && id.equals(b.getMaChiNhanh())) return b;
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

  private void addCombo(JPanel parent, JComboBox<OptionDto> combo, int x, int y, int w, int h) {
    styleCombo(combo);
    OutlinedInputPanel panel = new OutlinedInputPanel();
    panel.setLayout(new BorderLayout());
    panel.setBounds(x, y, w, h);
    panel.add(combo, BorderLayout.CENTER);
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
    t.getColumnModel().getColumn(0).setPreferredWidth(70);
    t.getColumnModel().getColumn(1).setPreferredWidth(150);
    t.getColumnModel().getColumn(2).setPreferredWidth(240);
    t.getColumnModel().getColumn(3).setPreferredWidth(110);
    t.getColumnModel().getColumn(4).setPreferredWidth(150);
    t.getColumnModel().getColumn(5).setPreferredWidth(70);
    t.getColumnModel().getColumn(6).setPreferredWidth(120);
    t.setDefaultRenderer(
        Object.class,
        new DefaultTableCellRenderer() {
          @Override
          public Component getTableCellRendererComponent(
              JTable table,
              Object value,
              boolean isSelected,
              boolean hasFocus,
              int row,
              int column) {
            Component c =
                super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            if (!isSelected) c.setForeground(statusColor(String.valueOf(value)));
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

  private void hideScrollBars(JScrollPane sp) {
    sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    sp.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
  }

  private void styleField(JTextField field) {
    field.setFont(UiTheme.regular(14));
    field.setForeground(TEXT);
    field.setBorder(
        BorderFactory.createEmptyBorder(
            0, field instanceof IconPlaceholderTextField ? 48 : 12, 0, 12));
    field.setOpaque(false);
  }

  private void styleArea(JTextArea area) {
    area.setFont(UiTheme.regular(14));
    area.setForeground(TEXT);
    area.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
    area.setLineWrap(true);
    area.setWrapStyleWord(true);
    area.setOpaque(false);
  }

  private void styleCombo(JComboBox<OptionDto> combo) {
    combo.setUI(new DesignComboBoxUI());
    combo.setRenderer(new DesignComboBoxRenderer());
    combo.setFont(UiTheme.regular(14));
    combo.setForeground(TEXT);
    combo.setBackground(WHITE);
    combo.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 8));
    combo.setOpaque(false);
    combo.setFocusable(false);
  }

  private void addLabel(JPanel parent, String text, int x, int y, int w, int h) {
    JLabel label = new JLabel(text);
    label.setBounds(x, y, w, h);
    label.setForeground(MUTED);
    label.setFont(UiTheme.regular(13));
    parent.add(label);
  }

  private JLabel sectionTitle(String text) {
    JLabel label = new JLabel(text);
    label.setForeground(PRIMARY_DARK);
    label.setFont(UiTheme.bold(16));
    return label;
  }

  private JLabel statValue() {
    JLabel label = new JLabel("0");
    label.setForeground(TEXT);
    label.setFont(UiTheme.bold(24));
    return label;
  }

  private String selectedCode(JComboBox<OptionDto> combo) {
    OptionDto o = (OptionDto) combo.getSelectedItem();
    return o == null ? null : o.code();
  }

  private void selectStatus(JComboBox<OptionDto> combo, String code) {
    if (code == null) {
      combo.setSelectedIndex(0);
      return;
    }
    for (int i = 0; i < combo.getItemCount(); i++)
      if (code.equals(combo.getItemAt(i).code())) {
        combo.setSelectedIndex(i);
        return;
      }
    combo.setSelectedIndex(0);
  }

  private String statusText(String status) {
    if ("ACTIVE".equalsIgnoreCase(status)) return "Đang hoạt động";
    if ("CLOSED".equalsIgnoreCase(status)) return "Đã đóng";
    if ("MAINTENANCE".equalsIgnoreCase(status)) return "Bảo trì";
    return valueOrDash(status);
  }

  private Color statusColor(String value) {
    if ("Đã đóng".equals(value)) return DANGER;
    if ("Bảo trì".equals(value)) return WARNING;
    if ("Đang hoạt động".equals(value)) return SUCCESS;
    return TEXT;
  }

  private static RoundedButton primaryButton(String text) {
    return new RoundedButton(text).background(PRIMARY).hover(PRIMARY_DARK).radius(10);
  }

  private static RoundedButton secondaryButton(String text) {
    RoundedButton b =
        new RoundedButton(text)
            .background(Color.decode("#B9B9B9"))
            .hover(Color.decode("#A8A8A8"))
            .radius(10);
    b.setForeground(TEXT);
    return b;
  }

  private void showWarning(String msg) {
    JOptionPane.showMessageDialog(this, msg, "Quản lý chi nhánh", JOptionPane.WARNING_MESSAGE);
  }

  private void showStatus(String msg, boolean error) {
    statusLabel.setForeground(error ? DANGER : SUCCESS);
    statusLabel.setText(msg);
  }

  private String unwrapMessage(Exception ex) {
    Throwable c = ex;
    while (c.getCause() != null) c = c.getCause();
    return c.getMessage() == null ? "Không xử lý được yêu cầu" : c.getMessage();
  }

  private String valueOrDash(String v) {
    return v == null || v.isBlank() ? "-" : v;
  }

  private String valueOrEmpty(String v) {
    return v == null ? "" : v;
  }

  private record OptionDto(String code, String name) {
    @Override
    public String toString() {
      return name;
    }
  }

  private static class RoundedCard extends JPanel {
    private final int radius;
    private final Color fill;
    private final Color border;

    RoundedCard(int radius, Color fill, Color border) {
      this.radius = radius;
      this.fill = fill;
      this.border = border;
      setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(fill);
      g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
      g2.setColor(border);
      g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, radius, radius);
      g2.dispose();
      super.paintComponent(g);
    }
  }

  private static class RoundedInputPanel extends JPanel {
    RoundedInputPanel() {
      setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(WHITE);
      g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
      g2.setColor(BORDER);
      g2.setStroke(new BasicStroke(1.2f));
      g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
      g2.dispose();
      super.paintComponent(g);
    }
  }

  private static class OutlinedInputPanel extends JPanel {
    OutlinedInputPanel() {
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
    }

    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(WHITE);
      g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
      g2.dispose();
      super.paintComponent(g);
    }

    @Override
    protected void paintChildren(Graphics g) {
      super.paintChildren(g);
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(BORDER);
      g2.setStroke(new BasicStroke(1.2f));
      g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);
      g2.dispose();
    }
  }

  private static class DesignComboBoxUI extends BasicComboBoxUI {
    @Override
    public void installUI(JComponent c) {
      super.installUI(c);
      c.setOpaque(false);
    }

    @Override
    protected JButton createArrowButton() {
      JButton button =
          new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
              Graphics2D g2 = (Graphics2D) g.create();
              g2.setRenderingHint(
                  RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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
    public void paintCurrentValueBackground(
        Graphics g, java.awt.Rectangle bounds, boolean hasFocus) {}
  }

  private static class DesignComboBoxRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(
        JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      JLabel label =
          (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      if (value instanceof OptionDto option) label.setText(option.name());
      label.setFont(UiTheme.regular(14));
      label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 28));
      label.setForeground(TEXT);
      label.setBackground(isSelected && index >= 0 ? Color.decode("#F8DCC6") : WHITE);
      label.setPreferredSize(new Dimension(0, 28));
      return label;
    }
  }

  private static class IconPlaceholderTextField extends JTextField {
    private final String placeholder;
    private final Icon icon;

    IconPlaceholderTextField(String placeholder, Icon icon) {
      this.placeholder = placeholder;
      this.icon = icon;
      setMargin(new Insets(0, 0, 0, 12));
    }

    @Override
    public Insets getInsets() {
      return new Insets(0, 48, 0, 12);
    }

    @Override
    public Insets getInsets(Insets insets) {
      insets.top = 0;
      insets.left = 48;
      insets.bottom = 0;
      insets.right = 12;
      return insets;
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      if (icon != null) icon.paintIcon(this, g2, 12, (getHeight() - icon.getIconHeight()) / 2);
      if (getText().isEmpty()) {
        g2.setColor(MUTED);
        g2.setFont(getFont());
        int y = getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 2;
        g2.drawString(placeholder, 48, y);
      }
      g2.dispose();
    }
  }
}
