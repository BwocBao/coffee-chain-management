package com.coffeechain.ui;

import com.coffeechain.service.IngredientApiClient;
import com.coffeechain.service.IngredientApiClient.IngredientDto;
import com.coffeechain.service.IngredientApiClient.IngredientLookupDto;
import com.coffeechain.service.IngredientApiClient.IngredientRequest;
import com.coffeechain.service.IngredientApiClient.OptionDto;
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
import java.math.BigDecimal;
import java.text.DecimalFormat;
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

/** Màn hình quản lý nguyên liệu. */
public class QuanLyNguyenLieuFrame extends JFrame {
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

  private final IngredientApiClient apiClient = new IngredientApiClient();
  private final JPanel root = new JPanel(null);
  private final JTextField searchField =
      new IconPlaceholderTextField("Tìm nguyên liệu", new FlatSVGIcon("icons/tim.svg", 18, 18));
  private final JComboBox<OptionDto> filterUnitCombo = new JComboBox<>();
  private final JComboBox<OptionDto> filterStatusCombo = new JComboBox<>();
  private final JTextField nameField = new JTextField();
  private final JComboBox<OptionDto> unitCombo = new JComboBox<>();
  private final JTextField minimumStockField = new JTextField();
  private final JComboBox<OptionDto> statusCombo = new JComboBox<>();
  private final JLabel statusLabel = new JLabel(" ");
  private final RoundedButton saveButton = primaryButton("Lưu");
  private final RoundedButton deactivateButton = dangerButton("Ngừng HĐ");
  private final RoundedButton clearButton = secondaryButton("Làm mới");
  private final DecimalFormat numberFormat = new DecimalFormat("#,##0.###");

  private final DefaultTableModel tableModel =
      new DefaultTableModel(
          new Object[] {
            "Mã NL", "Tên nguyên liệu", "Đơn vị", "Ký hiệu", "Tồn tối thiểu", "Trạng thái"
          },
          0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return false;
        }
      };
  private final JTable table = new JTable(tableModel);
  private final List<IngredientDto> ingredients = new ArrayList<>();
  private List<OptionDto> units = new ArrayList<>();
  private Long selectedIngredientId;
  private boolean loading;

  public QuanLyNguyenLieuFrame() {
    setTitle("Phụng Lộc - Quản lý nguyên liệu");
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
    JLabel title = new JLabel("QUẢN LÝ NGUYÊN LIỆU");
    title.setBounds(44, 28, 560, 44);
    title.setForeground(PRIMARY);
    title.setFont(UiTheme.bold(32));
    root.add(title);

    JLabel subtitle =
        new JLabel(
            "Theo dõi danh mục nguyên liệu, đơn vị tính, mức tồn tối thiểu và trạng thái sử dụng");
    subtitle.setBounds(44, 72, 760, 24);
    subtitle.setForeground(MUTED);
    subtitle.setFont(UiTheme.regular(14));
    root.add(subtitle);

    RoundedButton backButton = primaryButton("Quay lại");
    backButton.setIcon(IconLoader.svg("icons/nhap-kho/left.svg", 16, 18));
    backButton.setIconTextGap(8);
    backButton.setHorizontalAlignment(SwingConstants.CENTER);
    backButton.setBounds(1250, 34, 110, 34);
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
    addFieldPanel(card, searchField, 24, 40, 380, 36);
    addLabel(card, "Đơn vị tính", 430, 12, 120, 20);
    addCombo(card, filterUnitCombo, 430, 40, 220, 36);
    addLabel(card, "Trạng thái", 676, 12, 120, 20);
    addCombo(card, filterStatusCombo, 676, 40, 220, 36);

    RoundedButton searchButton = primaryButton("Lọc");
    searchButton.setBounds(926, 40, 72, 36);
    searchButton.addActionListener(e -> loadIngredients());
    card.add(searchButton);

    RoundedButton resetButton = secondaryButton("Reset");
    resetButton.setBounds(1012, 40, 82, 36);
    resetButton.addActionListener(e -> resetFilters());
    card.add(resetButton);

    RoundedButton addButton = primaryButton("Thêm mới");
    addButton.setBounds(1210, 40, 112, 36);
    addButton.addActionListener(e -> clearForm());
    card.add(addButton);
  }

  private void buildTable() {
    JLabel title = sectionTitle("DANH SÁCH NGUYÊN LIỆU");
    title.setBounds(44, 238, 360, 26);
    root.add(title);

    configureTable(table);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table
        .getSelectionModel()
        .addListSelectionListener(
            e -> {
              if (!e.getValueIsAdjusting()) {
                fillSelectedIngredient();
              }
            });

    JScrollPane scrollPane = hiddenScrollPane(table);
    scrollPane.setBounds(44, 272, 860, 340);
    root.add(scrollPane);
  }

  private void buildForm() {
    JLabel title = sectionTitle("THÔNG TIN NGUYÊN LIỆU");
    title.setBounds(944, 238, 360, 26);
    root.add(title);

    RoundedCard card = new RoundedCard(16, WHITE, SOFT_BORDER);
    card.setLayout(null);
    card.setBounds(944, 272, 452, 340);
    root.add(card);

    addLabel(card, "Tên nguyên liệu", 24, 34, 160, 20);
    addFieldPanel(card, nameField, 24, 58, 404, 36);
    addLabel(card, "Đơn vị tính", 24, 116, 160, 20);
    addCombo(card, unitCombo, 24, 140, 188, 36);
    addLabel(card, "Mức tồn tối thiểu", 240, 116, 180, 20);
    addFieldPanel(card, minimumStockField, 240, 140, 188, 36);
    addLabel(card, "Trạng thái", 24, 198, 160, 20);
    addCombo(card, statusCombo, 24, 222, 404, 36);

    saveButton.setBounds(24, 294, 96, 34);
    saveButton.addActionListener(e -> saveIngredient());
    card.add(saveButton);

    deactivateButton.setBounds(136, 294, 104, 34);
    deactivateButton.addActionListener(e -> deactivateIngredient());
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
    searchField.addActionListener(e -> loadIngredients());
  }

  private void loadLookupsAndData() {
    setButtonsEnabled(false);
    showStatus("Đang tải dữ liệu nguyên liệu...", false);
    new SwingWorker<IngredientLookupDto, Void>() {
      @Override
      protected IngredientLookupDto doInBackground() throws Exception {
        return apiClient.getLookups();
      }

      @Override
      protected void done() {
        try {
          IngredientLookupDto lookup = get();
          units =
              lookup == null || lookup.getUnits() == null ? new ArrayList<>() : lookup.getUnits();
          populateCombos();
          loadIngredients();
        } catch (Exception ex) {
          showStatus("Không tải được dữ liệu nguyên liệu: " + unwrapMessage(ex), true);
          JOptionPane.showMessageDialog(
              QuanLyNguyenLieuFrame.this, unwrapMessage(ex), "Lỗi", JOptionPane.ERROR_MESSAGE);
          setButtonsEnabled(true);
          applyPermissions();
        }
      }
    }.execute();
  }

  private void populateCombos() {
    filterUnitCombo.removeAllItems();
    filterUnitCombo.addItem(new OptionDto(null, "Tất cả đơn vị", null));
    for (OptionDto unit : units) {
      filterUnitCombo.addItem(unit);
    }

    unitCombo.removeAllItems();
    for (OptionDto unit : units) {
      unitCombo.addItem(unit);
    }

    filterStatusCombo.removeAllItems();
    filterStatusCombo.addItem(new OptionDto(null, "Tất cả trạng thái", null));
    filterStatusCombo.addItem(new OptionDto(null, "Đang hoạt động", "ACTIVE"));
    filterStatusCombo.addItem(new OptionDto(null, "Ngừng hoạt động", "INACTIVE"));

    statusCombo.removeAllItems();
    statusCombo.addItem(new OptionDto(null, "Đang hoạt động", "ACTIVE"));
    statusCombo.addItem(new OptionDto(null, "Ngừng hoạt động", "INACTIVE"));
  }

  private void loadIngredients() {
    if (loading) {
      return;
    }
    loading = true;
    setButtonsEnabled(false);
    showStatus("Đang tải danh sách nguyên liệu...", false);
    String keyword = searchField.getText().trim();
    Long unitId = selectedId(filterUnitCombo);
    String status = selectedDescription(filterStatusCombo);

    new SwingWorker<List<IngredientDto>, Void>() {
      @Override
      protected List<IngredientDto> doInBackground() throws Exception {
        return apiClient.searchIngredients(keyword, status, unitId);
      }

      @Override
      protected void done() {
        try {
          ingredients.clear();
          ingredients.addAll(get());
          populateTable();
          showStatus("Đã tải " + ingredients.size() + " nguyên liệu", false);
          if (selectedIngredientId != null) {
            reselectIngredient(selectedIngredientId);
          }
        } catch (Exception ex) {
          showStatus("Không tải được nguyên liệu: " + unwrapMessage(ex), true);
          JOptionPane.showMessageDialog(
              QuanLyNguyenLieuFrame.this, unwrapMessage(ex), "Lỗi", JOptionPane.ERROR_MESSAGE);
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
    for (IngredientDto ingredient : ingredients) {
      tableModel.addRow(
          new Object[] {
            ingredient.getMaNguyenLieu(),
            valueOrDash(ingredient.getTenNguyenLieu()),
            valueOrDash(ingredient.getTenDonViTinh()),
            valueOrDash(ingredient.getKyHieuDonViTinh()),
            formatNumber(ingredient.getMucTonToiThieu()),
            statusText(ingredient.getTrangThai())
          });
    }
  }

  private void fillSelectedIngredient() {
    int viewRow = table.getSelectedRow();
    if (viewRow < 0) {
      return;
    }
    Long id = (Long) tableModel.getValueAt(table.convertRowIndexToModel(viewRow), 0);
    IngredientDto ingredient = findIngredient(id);
    if (ingredient == null) {
      return;
    }
    selectedIngredientId = ingredient.getMaNguyenLieu();
    nameField.setText(valueOrEmpty(ingredient.getTenNguyenLieu()));
    selectComboById(unitCombo, ingredient.getMaDonViTinh());
    minimumStockField.setText(
        ingredient.getMucTonToiThieu() == null
            ? ""
            : ingredient.getMucTonToiThieu().stripTrailingZeros().toPlainString());
    selectComboByDescription(statusCombo, ingredient.getTrangThai());
    applyPermissions();
  }

  private void saveIngredient() {
    IngredientRequest request = buildRequest();
    if (request == null) {
      return;
    }
    boolean createMode = selectedIngredientId == null;
    setButtonsEnabled(false);
    showStatus(createMode ? "Đang tạo nguyên liệu..." : "Đang cập nhật nguyên liệu...", false);
    new SwingWorker<IngredientDto, Void>() {
      @Override
      protected IngredientDto doInBackground() throws Exception {
        return createMode
            ? apiClient.createIngredient(request)
            : apiClient.updateIngredient(selectedIngredientId, request);
      }

      @Override
      protected void done() {
        try {
          IngredientDto saved = get();
          selectedIngredientId = saved.getMaNguyenLieu();
          showStatus(createMode ? "Đã tạo nguyên liệu" : "Đã cập nhật nguyên liệu", false);
          loadIngredients();
        } catch (Exception ex) {
          showStatus("Lưu thất bại: " + unwrapMessage(ex), true);
          JOptionPane.showMessageDialog(
              QuanLyNguyenLieuFrame.this, unwrapMessage(ex), "Lỗi", JOptionPane.ERROR_MESSAGE);
          setButtonsEnabled(true);
          applyPermissions();
        }
      }
    }.execute();
  }

  private IngredientRequest buildRequest() {
    String name = nameField.getText().trim();
    Long unitId = selectedId(unitCombo);
    BigDecimal minimumStock = parseDecimal(minimumStockField.getText().trim());
    if (name.isBlank()) {
      showWarning("Vui lòng nhập tên nguyên liệu");
      nameField.requestFocusInWindow();
      return null;
    }
    if (unitId == null) {
      showWarning("Vui lòng chọn đơn vị tính");
      return null;
    }
    if (minimumStock == null) {
      showWarning("Mức tồn tối thiểu phải là số không âm");
      minimumStockField.requestFocusInWindow();
      return null;
    }
    IngredientRequest request = new IngredientRequest();
    request.setTenNguyenLieu(name);
    request.setMaDonViTinh(unitId);
    request.setMucTonToiThieu(minimumStock);
    request.setTrangThai(selectedIngredientId == null ? null : selectedDescription(statusCombo));
    return request;
  }

  private BigDecimal parseDecimal(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    try {
      BigDecimal value = new BigDecimal(raw.replace(",", ""));
      return value.compareTo(BigDecimal.ZERO) < 0 ? null : value;
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  private void deactivateIngredient() {
    if (selectedIngredientId == null) {
      showWarning("Vui lòng chọn nguyên liệu cần ngừng hoạt động");
      return;
    }
    int confirm =
        JOptionPane.showConfirmDialog(
            this,
            "Bạn muốn chuyển nguyên liệu đang chọn sang trạng thái ngừng hoạt động?",
            "Ngừng hoạt động nguyên liệu",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
    if (confirm != JOptionPane.YES_OPTION) {
      return;
    }
    Long id = selectedIngredientId;
    setButtonsEnabled(false);
    showStatus("Đang ngừng hoạt động nguyên liệu...", false);
    new SwingWorker<IngredientDto, Void>() {
      @Override
      protected IngredientDto doInBackground() throws Exception {
        return apiClient.deactivateIngredient(id);
      }

      @Override
      protected void done() {
        try {
          IngredientDto updated = get();
          selectedIngredientId = updated.getMaNguyenLieu();
          showStatus("Đã ngừng hoạt động nguyên liệu", false);
          loadIngredients();
        } catch (Exception ex) {
          showStatus("Ngừng hoạt động thất bại: " + unwrapMessage(ex), true);
          JOptionPane.showMessageDialog(
              QuanLyNguyenLieuFrame.this, unwrapMessage(ex), "Lỗi", JOptionPane.ERROR_MESSAGE);
          setButtonsEnabled(true);
          applyPermissions();
        }
      }
    }.execute();
  }

  private void clearForm() {
    selectedIngredientId = null;
    table.clearSelection();
    nameField.setText("");
    minimumStockField.setText("");
    if (unitCombo.getItemCount() > 0) {
      unitCombo.setSelectedIndex(0);
    }
    if (statusCombo.getItemCount() > 0) {
      statusCombo.setSelectedIndex(0);
    }
    showStatus("Sẵn sàng nhập nguyên liệu mới", false);
    applyPermissions();
    SwingUtilities.invokeLater(() -> nameField.requestFocusInWindow());
  }

  private void resetFilters() {
    searchField.setText("");
    if (filterUnitCombo.getItemCount() > 0) {
      filterUnitCombo.setSelectedIndex(0);
    }
    if (filterStatusCombo.getItemCount() > 0) {
      filterStatusCombo.setSelectedIndex(0);
    }
    loadIngredients();
  }

  private void applyPermissions() {
    boolean canCreate = PermissionUtil.hasAny("INGREDIENT:CREATE");
    boolean canUpdate = PermissionUtil.hasAny("INGREDIENT:UPDATE");
    boolean canDelete = PermissionUtil.hasAny("INGREDIENT:DELETE");
    saveButton.setEnabled(!loading && (selectedIngredientId == null ? canCreate : canUpdate));
    deactivateButton.setEnabled(!loading && selectedIngredientId != null && canDelete);
  }

  private void setButtonsEnabled(boolean enabled) {
    saveButton.setEnabled(enabled);
    deactivateButton.setEnabled(enabled);
    clearButton.setEnabled(enabled);
  }

  private void reselectIngredient(Long id) {
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

  private IngredientDto findIngredient(Long id) {
    for (IngredientDto ingredient : ingredients) {
      if (id != null && id.equals(ingredient.getMaNguyenLieu())) {
        return ingredient;
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
              c.setForeground("Ngừng hoạt động".equals(value) ? DANGER : TEXT);
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

  private void styleField(JTextField field) {
    field.setFont(UiTheme.regular(14));
    field.setForeground(TEXT);
    int leftInset = field instanceof IconPlaceholderTextField ? 48 : 12;
    field.setBorder(BorderFactory.createEmptyBorder(0, leftInset, 0, 12));
    field.setOpaque(false);
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
    combo.setMaximumRowCount(8);
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

  private Long selectedId(JComboBox<OptionDto> combo) {
    OptionDto option = (OptionDto) combo.getSelectedItem();
    return option == null ? null : option.getId();
  }

  private String selectedDescription(JComboBox<OptionDto> combo) {
    OptionDto option = (OptionDto) combo.getSelectedItem();
    return option == null ? null : option.getDescription();
  }

  private void selectComboById(JComboBox<OptionDto> combo, Long id) {
    if (id == null) {
      combo.setSelectedIndex(0);
      return;
    }
    for (int i = 0; i < combo.getItemCount(); i++) {
      OptionDto option = combo.getItemAt(i);
      if (id.equals(option.getId())) {
        combo.setSelectedIndex(i);
        return;
      }
    }
    combo.setSelectedIndex(0);
  }

  private void selectComboByDescription(JComboBox<OptionDto> combo, String description) {
    if (description == null) {
      combo.setSelectedIndex(0);
      return;
    }
    for (int i = 0; i < combo.getItemCount(); i++) {
      OptionDto option = combo.getItemAt(i);
      if (description.equals(option.getDescription())) {
        combo.setSelectedIndex(i);
        return;
      }
    }
    combo.setSelectedIndex(0);
  }

  private String statusText(String status) {
    if ("ACTIVE".equalsIgnoreCase(status)) {
      return "Đang hoạt động";
    }
    if ("INACTIVE".equalsIgnoreCase(status)) {
      return "Ngừng hoạt động";
    }
    return valueOrDash(status);
  }

  private String formatNumber(BigDecimal value) {
    return value == null ? "-" : numberFormat.format(value);
  }

  private static RoundedButton primaryButton(String text) {
    return new RoundedButton(text).background(PRIMARY).hover(PRIMARY_DARK).radius(10);
  }

  private static RoundedButton secondaryButton(String text) {
    RoundedButton button =
        new RoundedButton(text)
            .background(Color.decode("#B9B9B9"))
            .hover(Color.decode("#A8A8A8"))
            .radius(10);
    button.setForeground(TEXT);
    return button;
  }

  private static RoundedButton dangerButton(String text) {
    return new RoundedButton(text).background(DANGER).hover(Color.decode("#A83427")).radius(10);
  }

  private void showWarning(String msg) {
    JOptionPane.showMessageDialog(this, msg, "Quản lý nguyên liệu", JOptionPane.WARNING_MESSAGE);
  }

  private void showStatus(String msg, boolean error) {
    statusLabel.setForeground(error ? DANGER : SUCCESS);
    statusLabel.setText(msg);
  }

  private String unwrapMessage(Exception ex) {
    Throwable cause = ex;
    while (cause.getCause() != null) {
      cause = cause.getCause();
    }
    return cause.getMessage() == null ? "Không xử lý được yêu cầu" : cause.getMessage();
  }

  private String valueOrDash(String value) {
    return value == null || value.isBlank() ? "-" : value;
  }

  private String valueOrEmpty(String value) {
    return value == null ? "" : value;
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
      g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
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
      // OutlinedInputPanel vẽ nền và viền.
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
