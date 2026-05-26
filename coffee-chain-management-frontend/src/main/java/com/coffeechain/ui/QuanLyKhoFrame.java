package com.coffeechain.ui;

import com.coffeechain.service.WarehouseApiClient;
import com.coffeechain.service.WarehouseApiClient.OptionDto;
import com.coffeechain.service.WarehouseApiClient.WarehouseDto;
import com.coffeechain.service.WarehouseApiClient.WarehouseLookupDto;
import com.coffeechain.service.WarehouseApiClient.WarehouseRequest;
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
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/** Màn hình quản lý thông tin kho. */
public class QuanLyKhoFrame extends JFrame {

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

  private final WarehouseApiClient apiClient = new WarehouseApiClient();
  private final JPanel root = new JPanel(null);
  private final JTextField searchField =
      new IconPlaceholderTextField("Tìm kho", new FlatSVGIcon("icons/tim.svg", 18, 18));
  private final JComboBox<OptionDto> filterTypeCombo = new JComboBox<>();
  private final JComboBox<OptionDto> filterStatusCombo = new JComboBox<>();
  private final JTextField nameField = new JTextField();
  private final JComboBox<OptionDto> typeCombo = new JComboBox<>();
  private final JComboBox<OptionDto> branchCombo = new JComboBox<>();
  private final JComboBox<OptionDto> statusCombo = new JComboBox<>();
  //    private final JLabel selectedIdLabel = new JLabel("Chưa chọn kho");
  private final JLabel statusLabel = new JLabel(" ");
  private final RoundedButton saveButton = primaryButton("Lưu");
  private final RoundedButton deactivateButton = dangerButton("Ngưng HĐ");
  private final RoundedButton clearButton = secondaryButton("Làm mới");
  private final DefaultTableModel tableModel =
      new DefaultTableModel(
          new Object[] {"Mã kho", "Tên kho", "Loại kho", "Chi nhánh", "Trạng thái"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return false;
        }
      };
  private final JTable table = new JTable(tableModel);
  private final List<WarehouseDto> warehouses = new ArrayList<>();
  private List<OptionDto> warehouseTypes = new ArrayList<>();
  private List<OptionDto> branches = new ArrayList<>();
  private Long selectedWarehouseId;
  private boolean loading;

  public QuanLyKhoFrame() {
    setTitle("Phụng Lộc - Quản lý thông tin kho");
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
    loadLookupsAndData();
  }

  private void buildHeader() {
    JLabel title = new JLabel("QUẢN LÝ THÔNG TIN KHO");
    title.setBounds(44, 28, 560, 44);
    title.setForeground(PRIMARY);
    title.setFont(UiTheme.bold(32));
    root.add(title);
    JLabel subtitle =
        new JLabel("Theo dõi kho tổng, kho chi nhánh, trạng thái hoạt động và liên kết chi nhánh");
    subtitle.setBounds(44, 72, 760, 24);
    subtitle.setForeground(MUTED);
    subtitle.setFont(UiTheme.regular(14));
    root.add(subtitle);
    RoundedButton backButton = primaryButton("Quay lại");
    backButton.setIcon(IconLoader.svg("icons/nhap-kho/left.svg", 16, 18));
    backButton.setIconTextGap(8);
    backButton.setHorizontalAlignment(SwingConstants.CENTER);
    backButton.setBounds(1285, 34, 110, 34);
    backButton.addActionListener(
        e -> {
          new KhoMenuFrame().setVisible(true);
          dispose();
        });
    root.add(backButton);
  }

  private void buildToolbar() {
    RoundedCard card = new RoundedCard(16, WHITE, SOFT_BORDER);
    card.setLayout(null);
    card.setBounds(44, 122, 1352, 92);
    root.add(card);
    addLabel(card, "Tìm kiếm", 24, 12, 150, 20);
    addFieldPanel(card, searchField, 24, 40, 370, 36);
    addLabel(card, "Loại kho", 420, 12, 120, 20);
    addCombo(card, filterTypeCombo, 420, 40, 210, 36);
    addLabel(card, "Trạng thái", 656, 12, 120, 20);
    addCombo(card, filterStatusCombo, 656, 40, 220, 36);
    RoundedButton searchButton = primaryButton("Lọc");
    searchButton.setBounds(906, 40, 72, 36);
    searchButton.addActionListener(e -> loadWarehouses());
    card.add(searchButton);
    RoundedButton resetButton = secondaryButton("Reset");
    resetButton.setBounds(992, 40, 82, 36);
    resetButton.addActionListener(e -> resetFilters());
    card.add(resetButton);
    RoundedButton addButton = primaryButton("Thêm mới");
    addButton.setBounds(1210, 40, 112, 36);
    addButton.addActionListener(e -> clearForm());
    card.add(addButton);
  }

  private void buildTable() {
    JLabel title = sectionTitle("DANH SÁCH KHO");
    title.setBounds(44, 238, 360, 26);
    root.add(title);
    configureTable(table);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table
        .getSelectionModel()
        .addListSelectionListener(
            e -> {
              if (!e.getValueIsAdjusting()) {
                fillSelectedWarehouse();
              }
            });
    JScrollPane scrollPane = hiddenScrollPane(table);
    scrollPane.setBounds(44, 272, 860, 340);
    root.add(scrollPane);
  }

  private void buildForm() {
    JLabel title = sectionTitle("THÔNG TIN KHO");
    title.setBounds(944, 238, 360, 26);
    root.add(title);
    RoundedCard card = new RoundedCard(16, WHITE, SOFT_BORDER);
    card.setLayout(null);
    card.setBounds(944, 272, 452, 340);
    root.add(card);
    //        selectedIdLabel.setBounds(24, 16, 390, 22);
    //        selectedIdLabel.setForeground(MUTED);
    //        selectedIdLabel.setFont(UiTheme.regular(13));
    //        card.add(selectedIdLabel);
    addLabel(card, "Tên kho", 24, 34, 160, 20);
    addFieldPanel(card, nameField, 24, 58, 404, 36);
    addLabel(card, "Loại kho", 24, 116, 160, 20);
    addCombo(card, typeCombo, 24, 140, 168, 36);
    addLabel(card, "Trạng thái", 220, 116, 160, 20);
    addCombo(card, statusCombo, 220, 140, 208, 36);
    addLabel(card, "Chi nhánh", 24, 198, 160, 20);
    addCombo(card, branchCombo, 24, 222, 404, 36);
    saveButton.setBounds(24, 294, 96, 34);
    saveButton.addActionListener(e -> saveWarehouse());
    card.add(saveButton);
    deactivateButton.setBounds(136, 294, 104, 34);
    deactivateButton.addActionListener(e -> deactivateWarehouse());
    card.add(deactivateButton);
    clearButton.setBounds(256, 294, 110, 34);
    clearButton.addActionListener(e -> clearForm());
    card.add(clearButton);
    statusLabel.setBounds(44, 634, 860, 24);
    statusLabel.setForeground(MUTED);
    statusLabel.setFont(UiTheme.regular(13));
    root.add(statusLabel);
  }

  private void bindEvents() {
    searchField.addActionListener(e -> loadWarehouses());
    typeCombo.addActionListener(e -> updateBranchEnabled());
  }

  private void loadLookupsAndData() {
    setButtonsEnabled(false);
    showStatus("Đang tải dữ liệu kho...", false);
    new SwingWorker<WarehouseLookupDto, Void>() {
      @Override
      protected WarehouseLookupDto doInBackground() throws Exception {
        return apiClient.getLookups();
      }

      @Override
      protected void done() {
        try {
          WarehouseLookupDto lookup = get();
          warehouseTypes =
              lookup.getWarehouseTypes() == null ? new ArrayList<>() : lookup.getWarehouseTypes();
          branches = lookup.getBranches() == null ? new ArrayList<>() : lookup.getBranches();
          populateCombos();
          loadWarehouses();
        } catch (Exception ex) {
          showStatus("Không tải được dữ liệu kho: " + unwrapMessage(ex), true);
          JOptionPane.showMessageDialog(
              QuanLyKhoFrame.this, unwrapMessage(ex), "Lỗi", JOptionPane.ERROR_MESSAGE);
          setButtonsEnabled(true);
          applyPermissions();
        }
      }
    }.execute();
  }

  private void populateCombos() {
    filterTypeCombo.removeAllItems();
    filterTypeCombo.addItem(new OptionDto(null, null, "Tất cả loại kho", null));
    for (OptionDto type : warehouseTypes) {
      filterTypeCombo.addItem(type);
    }
    typeCombo.removeAllItems();
    for (OptionDto type : warehouseTypes) {
      typeCombo.addItem(type);
    }
    filterStatusCombo.removeAllItems();
    filterStatusCombo.addItem(new OptionDto(null, null, "Tất cả trạng thái", null));
    filterStatusCombo.addItem(new OptionDto(null, "ACTIVE", "Đang hoạt động", null));
    filterStatusCombo.addItem(new OptionDto(null, "INACTIVE", "Ngưng hoạt động", null));
    statusCombo.removeAllItems();
    statusCombo.addItem(new OptionDto(null, "ACTIVE", "Đang hoạt động", null));
    statusCombo.addItem(new OptionDto(null, "INACTIVE", "Ngưng hoạt động", null));
    branchCombo.removeAllItems();
    branchCombo.addItem(new OptionDto(null, null, "Không gắn chi nhánh", null));
    for (OptionDto branch : branches) {
      branchCombo.addItem(branch);
    }
    updateBranchEnabled();
  }

  private void loadWarehouses() {
    if (loading) {
      return;
    }
    loading = true;
    setButtonsEnabled(false);
    showStatus("Đang tải danh sách kho...", false);
    String keyword = searchField.getText().trim();
    String loaiKho = selectedCode(filterTypeCombo);
    String trangThai = selectedCode(filterStatusCombo);
    new SwingWorker<List<WarehouseDto>, Void>() {
      @Override
      protected List<WarehouseDto> doInBackground() throws Exception {
        return apiClient.searchWarehouses(keyword, loaiKho, trangThai);
      }

      @Override
      protected void done() {
        try {
          warehouses.clear();
          warehouses.addAll(get());
          populateTable();
          showStatus("Đã tải " + warehouses.size() + " kho", false);
          if (selectedWarehouseId != null) {
            reselectWarehouse(selectedWarehouseId);
          }
        } catch (Exception ex) {
          showStatus("Không tải được kho: " + unwrapMessage(ex), true);
          JOptionPane.showMessageDialog(
              QuanLyKhoFrame.this, unwrapMessage(ex), "Lỗi", JOptionPane.ERROR_MESSAGE);
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
    for (WarehouseDto w : warehouses) {
      tableModel.addRow(
          new Object[] {
            w.getMaKho(),
            valueOrDash(w.getTenKho()),
            typeLabel(w.getLoaiKho()),
            valueOrDash(w.getTenChiNhanh()),
            statusText(w.getTrangThai())
          });
    }
  }

  private void fillSelectedWarehouse() {
    int viewRow = table.getSelectedRow();
    if (viewRow < 0) {
      return;
    }
    Long id = (Long) tableModel.getValueAt(table.convertRowIndexToModel(viewRow), 0);
    WarehouseDto w = findWarehouse(id);
    if (w == null) {
      return;
    }
    selectedWarehouseId = w.getMaKho();
    //        selectedIdLabel.setText("Đang sửa kho #" + selectedWarehouseId);
    nameField.setText(valueOrEmpty(w.getTenKho()));
    selectComboByCode(typeCombo, w.getLoaiKho());
    selectComboByCode(statusCombo, w.getTrangThai());
    selectComboById(branchCombo, w.getMaChiNhanh());
    updateBranchEnabled();
    applyPermissions();
  }

  private void saveWarehouse() {
    WarehouseRequest request = buildRequest();
    if (request == null) {
      return;
    }
    boolean createMode = selectedWarehouseId == null;
    setButtonsEnabled(false);
    showStatus(createMode ? "Đang tạo kho..." : "Đang cập nhật kho...", false);
    new SwingWorker<WarehouseDto, Void>() {
      @Override
      protected WarehouseDto doInBackground() throws Exception {
        return createMode
            ? apiClient.createWarehouse(request)
            : apiClient.updateWarehouse(selectedWarehouseId, request);
      }

      @Override
      protected void done() {
        try {
          WarehouseDto saved = get();
          selectedWarehouseId = saved.getMaKho();
          showStatus(createMode ? "Đã tạo kho" : "Đã cập nhật kho", false);
          loadWarehouses();
        } catch (Exception ex) {
          showStatus("Lưu thất bại: " + unwrapMessage(ex), true);
          JOptionPane.showMessageDialog(
              QuanLyKhoFrame.this, unwrapMessage(ex), "Lỗi", JOptionPane.ERROR_MESSAGE);
          setButtonsEnabled(true);
          applyPermissions();
        }
      }
    }.execute();
  }

  private WarehouseRequest buildRequest() {
    String name = nameField.getText().trim();
    String type = selectedCode(typeCombo);
    String status = selectedCode(statusCombo);
    Long branchId = selectedId(branchCombo);
    if (name.isBlank()) {
      showWarning("Vui lòng nhập tên kho");
      nameField.requestFocusInWindow();
      return null;
    }
    if (type == null || type.isBlank()) {
      showWarning("Vui lòng chọn loại kho");
      return null;
    }
    if ("CENTRAL".equals(type)) {
      branchId = null;
    } else if (branchId == null) {
      showWarning("Kho chi nhánh phải chọn chi nhánh");
      return null;
    }
    WarehouseRequest request = new WarehouseRequest();
    request.setTenKho(name);
    request.setLoaiKho(type);
    request.setMaChiNhanh(branchId);
    request.setTrangThai(selectedWarehouseId == null ? null : status);
    return request;
  }

  private void deactivateWarehouse() {
    if (selectedWarehouseId == null) {
      showWarning("Vui lòng chọn kho cần ngưng hoạt động");
      return;
    }
    int confirm =
        JOptionPane.showConfirmDialog(
            this,
            "Bạn muốn chuyển kho đang chọn sang trạng thái ngưng hoạt động?",
            "Ngưng hoạt động kho",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
    if (confirm != JOptionPane.YES_OPTION) {
      return;
    }
    Long id = selectedWarehouseId;
    setButtonsEnabled(false);
    showStatus("Đang ngưng hoạt động kho...", false);
    new SwingWorker<WarehouseDto, Void>() {
      @Override
      protected WarehouseDto doInBackground() throws Exception {
        return apiClient.deactivateWarehouse(id);
      }

      @Override
      protected void done() {
        try {
          WarehouseDto updated = get();
          selectedWarehouseId = updated.getMaKho();
          showStatus("Đã ngưng hoạt động kho", false);
          loadWarehouses();
        } catch (Exception ex) {
          showStatus("Ngưng hoạt động thất bại: " + unwrapMessage(ex), true);
          JOptionPane.showMessageDialog(
              QuanLyKhoFrame.this, unwrapMessage(ex), "Lỗi", JOptionPane.ERROR_MESSAGE);
          setButtonsEnabled(true);
          applyPermissions();
        }
      }
    }.execute();
  }

  private void clearForm() {
    selectedWarehouseId = null;
    table.clearSelection();
    //        selectedIdLabel.setText("Chưa chọn kho");
    nameField.setText("");
    if (typeCombo.getItemCount() > 0) {
      typeCombo.setSelectedIndex(0);
    }
    if (statusCombo.getItemCount() > 0) {
      statusCombo.setSelectedIndex(0);
    }
    if (branchCombo.getItemCount() > 0) {
      branchCombo.setSelectedIndex(0);
    }
    updateBranchEnabled();
    showStatus("Sẵn sàng nhập kho mới", false);
    applyPermissions();
    SwingUtilities.invokeLater(() -> nameField.requestFocusInWindow());
  }

  private void resetFilters() {
    searchField.setText("");
    if (filterTypeCombo.getItemCount() > 0) {
      filterTypeCombo.setSelectedIndex(0);
    }
    if (filterStatusCombo.getItemCount() > 0) {
      filterStatusCombo.setSelectedIndex(0);
    }
    loadWarehouses();
  }

  private void updateBranchEnabled() {
    boolean isBranch = "BRANCH".equals(selectedCode(typeCombo));
    branchCombo.setEnabled(isBranch);
    if (!isBranch && branchCombo.getItemCount() > 0) {
      branchCombo.setSelectedIndex(0);
    }
  }

  private void applyPermissions() {
    boolean canCreate = PermissionUtil.hasAny("WAREHOUSE:CREATE");
    boolean canUpdate = PermissionUtil.hasAny("WAREHOUSE:UPDATE");
    boolean canDelete = PermissionUtil.hasAny("WAREHOUSE:DELETE");
    saveButton.setEnabled(!loading && (selectedWarehouseId == null ? canCreate : canUpdate));
    deactivateButton.setEnabled(!loading && selectedWarehouseId != null && canDelete);
  }

  private void setButtonsEnabled(boolean enabled) {
    saveButton.setEnabled(enabled);
    deactivateButton.setEnabled(enabled);
    clearButton.setEnabled(enabled);
  }

  private void reselectWarehouse(Long id) {
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

  private WarehouseDto findWarehouse(Long id) {
    for (WarehouseDto w : warehouses) {
      if (id != null && id.equals(w.getMaKho())) {
        return w;
      }
    }
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
    t.getColumnModel().getColumn(1).setPreferredWidth(260);
    t.getColumnModel().getColumn(2).setPreferredWidth(120);
    t.getColumnModel().getColumn(3).setPreferredWidth(220);
    t.getColumnModel().getColumn(4).setPreferredWidth(150);
    DefaultTableCellRenderer renderer =
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
            if (!isSelected) {
              c.setForeground("Ngưng hoạt động".equals(value) ? DANGER : TEXT);
            }
            return c;
          }
        };
    t.setDefaultRenderer(Object.class, renderer);
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
    sp.getHorizontalScrollBar().setUnitIncrement(34);
    return sp;
  }

  private void styleField(JTextField f) {
    f.setFont(UiTheme.regular(14));
    f.setForeground(TEXT);
    int leftInset = f instanceof IconPlaceholderTextField ? 48 : 12;
    f.setBorder(BorderFactory.createEmptyBorder(0, leftInset, 0, 12));
    f.setOpaque(false);
  }

  private void styleCombo(JComboBox<OptionDto> c) {
    c.setUI(new DesignComboBoxUI());
    c.setRenderer(new DesignComboBoxRenderer());
    c.setFont(UiTheme.regular(14));
    c.setForeground(TEXT);
    c.setBackground(WHITE);
    c.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 8));
    c.setOpaque(false);
    c.setFocusable(false);
    c.setMaximumRowCount(8);
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

  private String selectedCode(JComboBox<OptionDto> combo) {
    OptionDto o = (OptionDto) combo.getSelectedItem();
    return o == null ? null : o.getCode();
  }

  private Long selectedId(JComboBox<OptionDto> combo) {
    OptionDto o = (OptionDto) combo.getSelectedItem();
    return o == null ? null : o.getId();
  }

  private void selectComboByCode(JComboBox<OptionDto> combo, String code) {
    if (code == null) {
      combo.setSelectedIndex(0);
      return;
    }
    for (int i = 0; i < combo.getItemCount(); i++) {
      OptionDto o = combo.getItemAt(i);
      if (code.equals(o.getCode())) {
        combo.setSelectedIndex(i);
        return;
      }
    }
  }

  private void selectComboById(JComboBox<OptionDto> combo, Long id) {
    if (id == null) {
      combo.setSelectedIndex(0);
      return;
    }
    for (int i = 0; i < combo.getItemCount(); i++) {
      OptionDto o = combo.getItemAt(i);
      if (id.equals(o.getId())) {
        combo.setSelectedIndex(i);
        return;
      }
    }
    combo.setSelectedIndex(0);
  }

  private String typeLabel(String type) {
    if ("CENTRAL".equalsIgnoreCase(type)) {
      return "Kho tổng";
    }
    if ("BRANCH".equalsIgnoreCase(type)) {
      return "Kho chi nhánh";
    }
    return valueOrDash(type);
  }

  private String statusText(String status) {
    if ("ACTIVE".equalsIgnoreCase(status)) {
      return "Đang hoạt động";
    }
    if ("INACTIVE".equalsIgnoreCase(status)) {
      return "Ngưng hoạt động";
    }
    return valueOrDash(status);
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

  private static RoundedButton dangerButton(String text) {
    return new RoundedButton(text).background(DANGER).hover(Color.decode("#A83427")).radius(10);
  }

  private void showWarning(String msg) {
    JOptionPane.showMessageDialog(this, msg, "Quản lý kho", JOptionPane.WARNING_MESSAGE);
  }

  private void showStatus(String msg, boolean error) {
    statusLabel.setForeground(error ? DANGER : SUCCESS);
    statusLabel.setText(msg);
  }

  private String unwrapMessage(Exception ex) {
    Throwable c = ex;
    while (c.getCause() != null) {
      c = c.getCause();
    }
    return c.getMessage() == null ? "Không xử lý được yêu cầu" : c.getMessage();
  }

  private String valueOrDash(String v) {
    return v == null || v.isBlank() ? "-" : v;
  }

  private String valueOrEmpty(String v) {
    return v == null ? "" : v;
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
      g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
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
        Graphics g, java.awt.Rectangle bounds, boolean hasFocus) {
      // OutlinedInputPanel đã vẽ nền và viền theo mockup.
    }
  }

  private static class DesignComboBoxRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(
        JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      JLabel label =
          (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      if (value instanceof OptionDto option) {
        label.setText(option.getName());
      }
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
      if (icon != null) {
        icon.paintIcon(this, g2, 12, (getHeight() - icon.getIconHeight()) / 2);
      }
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
