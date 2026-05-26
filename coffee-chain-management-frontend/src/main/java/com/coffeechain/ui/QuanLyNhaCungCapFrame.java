package com.coffeechain.ui;

import com.coffeechain.service.SupplierApiClient;
import com.coffeechain.service.SupplierApiClient.SupplierDto;
import com.coffeechain.service.SupplierApiClient.SupplierRequest;
import com.coffeechain.ui.common.IconLoader;
import com.coffeechain.ui.common.PermissionUtil;
import com.coffeechain.ui.common.RoundedButton;
import com.coffeechain.ui.common.UiTheme;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/** Màn hình quản lý nhà cung cấp: tìm kiếm, thêm, sửa và xóa nhà cung cấp. */
public class QuanLyNhaCungCapFrame extends JFrame {
  private static final int ROOT_W = 1440;
  private static final int ROOT_H = 780;

  private static final Color WHITE = Color.WHITE;
  private static final Color PAGE_BG = Color.decode("#FFFFFF");
  private static final Color PRIMARY = UiTheme.PRIMARY;
  private static final Color PRIMARY_DARK = UiTheme.PRIMARY_DARK;
  private static final Color TEXT = UiTheme.TEXT_DARK;
  private static final Color MUTED = UiTheme.TEXT_MUTED;
  private static final Color BORDER = Color.decode("#B9B9B9");
  private static final Color SOFT_BORDER = Color.decode("#E6E6E6");
  private static final Color TABLE_HEAD = Color.decode("#F4E8DA");
  private static final Color FIELD_FILL = Color.WHITE;
  private static final Color DANGER = Color.decode("#BE3C2D");
  private static final Color SUCCESS = Color.decode("#3C8C5A");
  private static final String SEARCH_PLACEHOLDER = "Tìm nhà cung cấp";

  private final SupplierApiClient apiClient = new SupplierApiClient();
  private final JPanel root = new JPanel(null);

  private final JTextField searchField = createSearchField();
  private final JTextField nameField = new JTextField();
  private final JTextField phoneField = new JTextField();
  private final JTextField emailField = new JTextField();
  private final JTextArea addressArea = new JTextArea();
  //    private final JLabel selectedIdLabel = new JLabel("Chưa chọn nhà cung cấp");
  private final JLabel statusLabel = new JLabel(" ");

  private final RoundedButton saveButton = primaryButton("Lưu");
  private final RoundedButton deleteButton = dangerButton("Xóa");
  private final RoundedButton clearButton = secondaryButton("Làm mới");

  private final DefaultTableModel supplierTableModel =
      new DefaultTableModel(
          new Object[] {"Mã NCC", "Tên nhà cung cấp", "Số điện thoại", "Email", "Địa chỉ"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return false;
        }
      };
  private final JTable supplierTable = new JTable(supplierTableModel);

  private final List<SupplierDto> suppliers = new ArrayList<>();
  private Long selectedSupplierId;
  private boolean loading;

  public QuanLyNhaCungCapFrame() {
    setTitle("Phụng Lộc - Quản lý nhà cung cấp");
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setResizable(false);

    root.setPreferredSize(new Dimension(ROOT_W, ROOT_H));
    root.setBackground(PAGE_BG);
    setContentPane(root);

    buildHeader();
    buildToolbar();
    buildTable();
    buildForm();
    bindSearchOnEnter();
    applyPermissions();

    pack();
    setLocationRelativeTo(null);
    loadSuppliers();
  }

  private void buildHeader() {
    JLabel title = new JLabel("QUẢN LÝ NHÀ CUNG CẤP");
    title.setBounds(44, 28, 560, 44);
    title.setForeground(PRIMARY);
    title.setFont(UiTheme.bold(32));
    root.add(title);

    JLabel subtitle =
        new JLabel("Theo dõi thông tin liên hệ và địa chỉ của các nhà cung cấp nguyên liệu");
    subtitle.setBounds(44, 72, 680, 24);
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
    card.setBounds(44, 122, 1352, 86);
    root.add(card);

    JLabel searchLabel = label("Tìm kiếm");
    searchLabel.setBounds(24, 12, 160, 20);
    card.add(searchLabel);

    OutlinedInputPanel searchPanel = new OutlinedInputPanel();
    searchPanel.setLayout(new BorderLayout());
    searchPanel.setBounds(24, 38, 520, 36);
    searchPanel.add(searchField, BorderLayout.CENTER);
    card.add(searchPanel);

    RoundedButton searchButton = primaryButton("Tìm");
    searchButton.setBounds(562, 38, 76, 36);
    searchButton.addActionListener(e -> loadSuppliers());
    card.add(searchButton);

    RoundedButton resetButton = secondaryButton("Reset");
    resetButton.setBounds(654, 38, 82, 36);
    resetButton.addActionListener(
        e -> {
          searchField.setText("");
          loadSuppliers();
        });
    card.add(resetButton);

    RoundedButton addButton = primaryButton("Thêm mới");
    addButton.setBounds(1210, 38, 112, 36);
    addButton.addActionListener(e -> clearForm());
    card.add(addButton);
  }

  private void buildTable() {
    JLabel title = sectionTitle("DANH SÁCH NHÀ CUNG CẤP");
    title.setBounds(44, 232, 360, 26);
    root.add(title);

    configureTable(supplierTable);
    supplierTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    supplierTable
        .getSelectionModel()
        .addListSelectionListener(
            e -> {
              if (!e.getValueIsAdjusting()) {
                fillSelectedSupplier();
              }
            });

    JScrollPane scrollPane = hiddenScrollPane(supplierTable);
    scrollPane.setBounds(44, 266, 860, 340);
    root.add(scrollPane);
  }

  private void buildForm() {
    JLabel title = sectionTitle("THÔNG TIN NHÀ CUNG CẤP");
    title.setBounds(944, 232, 360, 26);
    root.add(title);

    RoundedCard card = new RoundedCard(16, WHITE, SOFT_BORDER);
    card.setLayout(null);
    card.setBounds(944, 266, 452, 340);
    root.add(card);

    //        selectedIdLabel.setBounds(24, 16, 390, 22);
    //        selectedIdLabel.setForeground(MUTED);
    //        selectedIdLabel.setFont(UiTheme.regular(13));
    //        card.add(selectedIdLabel);

    addInput(card, "Tên nhà cung cấp", nameField, 24, 52, 404, 36);
    addInput(card, "Số điện thoại", phoneField, 24, 116, 188, 36);
    addInput(card, "Email", emailField, 240, 116, 188, 36);

    JLabel addressLabel = label("Địa chỉ");
    addressLabel.setBounds(24, 176, 160, 20);
    card.add(addressLabel);

    styleArea(addressArea);
    OutlinedInputPanel addressPanel = new OutlinedInputPanel();
    addressPanel.setLayout(new BorderLayout());
    addressPanel.setBounds(24, 200, 404, 74);
    JScrollPane addressScroll = new JScrollPane(addressArea);
    addressScroll.setBorder(null);
    addressScroll.setOpaque(false);
    addressScroll.getViewport().setOpaque(false);
    hideScrollBarsButKeepWheel(addressScroll);
    addressPanel.add(addressScroll, BorderLayout.CENTER);
    card.add(addressPanel);

    saveButton.setBounds(24, 294, 96, 34);
    saveButton.addActionListener(e -> saveSupplier());
    card.add(saveButton);

    deleteButton.setBounds(136, 294, 96, 34);
    deleteButton.addActionListener(e -> deleteSupplier());
    card.add(deleteButton);

    clearButton.setBounds(248, 294, 110, 34);
    clearButton.addActionListener(e -> clearForm());
    card.add(clearButton);

    statusLabel.setBounds(44, 628, 860, 24);
    statusLabel.setForeground(MUTED);
    statusLabel.setFont(UiTheme.regular(13));
    root.add(statusLabel);
  }

  private void bindSearchOnEnter() {
    searchField.addActionListener(e -> loadSuppliers());
    searchField
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {
              @Override
              public void insertUpdate(DocumentEvent e) {
                repaintSearchField();
              }

              @Override
              public void removeUpdate(DocumentEvent e) {
                repaintSearchField();
              }

              @Override
              public void changedUpdate(DocumentEvent e) {
                repaintSearchField();
              }
            });
  }

  private void repaintSearchField() {
    searchField.repaint();
  }

  private void loadSuppliers() {
    if (loading) {
      return;
    }
    loading = true;
    setButtonsEnabled(false);
    showStatus("Đang tải danh sách nhà cung cấp...", false);
    String keyword = searchField.getText().trim();

    new SwingWorker<List<SupplierDto>, Void>() {
      @Override
      protected List<SupplierDto> doInBackground() throws Exception {
        return apiClient.searchSuppliers(keyword);
      }

      @Override
      protected void done() {
        try {
          suppliers.clear();
          suppliers.addAll(get());
          populateTable();
          showStatus("Đã tải " + suppliers.size() + " nhà cung cấp", false);
          if (selectedSupplierId != null) {
            reselectSupplier(selectedSupplierId);
          }
        } catch (Exception ex) {
          showStatus("Không tải được nhà cung cấp: " + unwrapMessage(ex), true);
          JOptionPane.showMessageDialog(
              QuanLyNhaCungCapFrame.this, unwrapMessage(ex), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } finally {
          loading = false;
          setButtonsEnabled(true);
          applyPermissions();
        }
      }
    }.execute();
  }

  private void populateTable() {
    supplierTableModel.setRowCount(0);
    for (SupplierDto supplier : suppliers) {
      supplierTableModel.addRow(
          new Object[] {
            supplier.getMaNhaCungCap(),
            valueOrDash(supplier.getTenNhaCungCap()),
            valueOrDash(supplier.getSoDienThoai()),
            valueOrDash(supplier.getEmail()),
            valueOrDash(supplier.getDiaChi())
          });
    }
  }

  private void fillSelectedSupplier() {
    int viewRow = supplierTable.getSelectedRow();
    if (viewRow < 0) {
      return;
    }
    int row = supplierTable.convertRowIndexToModel(viewRow);
    Long id = (Long) supplierTableModel.getValueAt(row, 0);
    SupplierDto supplier = findSupplier(id);
    if (supplier == null) {
      return;
    }

    selectedSupplierId = supplier.getMaNhaCungCap();
    //        selectedIdLabel.setText("Đang sửa NCC #" + selectedSupplierId);
    nameField.setText(valueOrEmpty(supplier.getTenNhaCungCap()));
    phoneField.setText(valueOrEmpty(supplier.getSoDienThoai()));
    emailField.setText(valueOrEmpty(supplier.getEmail()));
    addressArea.setText(valueOrEmpty(supplier.getDiaChi()));
    applyPermissions();
  }

  private void saveSupplier() {
    SupplierRequest request = buildRequest();
    if (request == null) {
      return;
    }

    boolean createMode = selectedSupplierId == null;
    setButtonsEnabled(false);
    showStatus(createMode ? "Đang tạo nhà cung cấp..." : "Đang cập nhật nhà cung cấp...", false);

    new SwingWorker<SupplierDto, Void>() {
      @Override
      protected SupplierDto doInBackground() throws Exception {
        if (createMode) {
          return apiClient.createSupplier(request);
        }
        return apiClient.updateSupplier(selectedSupplierId, request);
      }

      @Override
      protected void done() {
        try {
          SupplierDto saved = get();
          selectedSupplierId = saved.getMaNhaCungCap();
          showStatus(createMode ? "Đã tạo nhà cung cấp" : "Đã cập nhật nhà cung cấp", false);
          loadSuppliers();
        } catch (Exception ex) {
          showStatus("Lưu thất bại: " + unwrapMessage(ex), true);
          JOptionPane.showMessageDialog(
              QuanLyNhaCungCapFrame.this, unwrapMessage(ex), "Lỗi", JOptionPane.ERROR_MESSAGE);
          setButtonsEnabled(true);
          applyPermissions();
        }
      }
    }.execute();
  }

  private SupplierRequest buildRequest() {
    String name = nameField.getText().trim();
    String phone = phoneField.getText().trim();
    String email = emailField.getText().trim();
    String address = addressArea.getText().trim();

    if (name.isBlank()) {
      showWarning("Vui lòng nhập tên nhà cung cấp");
      nameField.requestFocusInWindow();
      return null;
    }
    if (phone.isBlank()) {
      showWarning("Vui lòng nhập số điện thoại");
      phoneField.requestFocusInWindow();
      return null;
    }
    if (email.isBlank()) {
      showWarning("Vui lòng nhập email");
      emailField.requestFocusInWindow();
      return null;
    }
    if (address.isBlank()) {
      showWarning("Vui lòng nhập địa chỉ");
      addressArea.requestFocusInWindow();
      return null;
    }

    SupplierRequest request = new SupplierRequest();
    request.setTenNhaCungCap(name);
    request.setSoDienThoai(phone);
    request.setEmail(email);
    request.setDiaChi(address);
    return request;
  }

  private void deleteSupplier() {
    if (selectedSupplierId == null) {
      showWarning("Vui lòng chọn nhà cung cấp cần xóa");
      return;
    }

    int confirm =
        JOptionPane.showConfirmDialog(
            this,
            "Bạn muốn xóa nhà cung cấp đang chọn?\nChỉ xóa được nếu chưa phát sinh phiếu nhập.",
            "Xóa nhà cung cấp",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
    if (confirm != JOptionPane.YES_OPTION) {
      return;
    }

    Long id = selectedSupplierId;
    setButtonsEnabled(false);
    showStatus("Đang xóa nhà cung cấp...", false);

    new SwingWorker<Void, Void>() {
      @Override
      protected Void doInBackground() throws Exception {
        apiClient.deleteSupplier(id);
        return null;
      }

      @Override
      protected void done() {
        try {
          get();
          clearForm();
          showStatus("Đã xóa nhà cung cấp", false);
          loadSuppliers();
        } catch (Exception ex) {
          showStatus("Xóa thất bại: " + unwrapMessage(ex), true);
          JOptionPane.showMessageDialog(
              QuanLyNhaCungCapFrame.this, unwrapMessage(ex), "Lỗi", JOptionPane.ERROR_MESSAGE);
          setButtonsEnabled(true);
          applyPermissions();
        }
      }
    }.execute();
  }

  private void clearForm() {
    selectedSupplierId = null;
    supplierTable.clearSelection();
    //        selectedIdLabel.setText("Chưa chọn nhà cung cấp");
    nameField.setText("");
    phoneField.setText("");
    emailField.setText("");
    addressArea.setText("");
    showStatus("Sẵn sàng nhập nhà cung cấp mới", false);
    applyPermissions();
    SwingUtilities.invokeLater(() -> nameField.requestFocusInWindow());
  }

  private void reselectSupplier(Long id) {
    for (int row = 0; row < supplierTableModel.getRowCount(); row++) {
      if (id.equals(supplierTableModel.getValueAt(row, 0))) {
        int viewRow = supplierTable.convertRowIndexToView(row);
        supplierTable.setRowSelectionInterval(viewRow, viewRow);
        supplierTable.scrollRectToVisible(supplierTable.getCellRect(viewRow, 0, true));
        return;
      }
    }
    clearForm();
  }

  private SupplierDto findSupplier(Long id) {
    for (SupplierDto supplier : suppliers) {
      if (id != null && id.equals(supplier.getMaNhaCungCap())) {
        return supplier;
      }
    }
    return null;
  }

  private void applyPermissions() {
    boolean canCreate = PermissionUtil.hasAny("SUPPLIER:CREATE");
    boolean canUpdate = PermissionUtil.hasAny("SUPPLIER:UPDATE");
    boolean canDelete = PermissionUtil.hasAny("SUPPLIER:DELETE");

    saveButton.setEnabled(!loading && (selectedSupplierId == null ? canCreate : canUpdate));
    deleteButton.setEnabled(!loading && selectedSupplierId != null && canDelete);
  }

  private void setButtonsEnabled(boolean enabled) {
    saveButton.setEnabled(enabled);
    deleteButton.setEnabled(enabled);
    clearButton.setEnabled(enabled);
  }

  private void addInput(
      JPanel parent, String labelText, JTextField field, int x, int y, int w, int h) {
    JLabel label = label(labelText);
    label.setBounds(x, y - 24, w, 20);
    parent.add(label);

    styleField(field);
    OutlinedInputPanel panel = new OutlinedInputPanel();
    panel.setLayout(new BorderLayout());
    panel.setBounds(x, y, w, h);
    panel.add(field, BorderLayout.CENTER);
    parent.add(panel);
  }

  private void configureTable(JTable table) {
    table.setRowHeight(36);
    table.setFont(UiTheme.regular(13));
    table.getTableHeader().setFont(UiTheme.bold(13));
    table.getTableHeader().setBackground(TABLE_HEAD);
    table.getTableHeader().setForeground(TEXT);
    table.getTableHeader().setPreferredSize(new Dimension(0, 36));
    table.setSelectionBackground(Color.decode("#F8DCC6"));
    table.setSelectionForeground(TEXT);
    table.setGridColor(SOFT_BORDER);
    table.setShowGrid(true);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

    table.getColumnModel().getColumn(0).setPreferredWidth(70);
    table.getColumnModel().getColumn(1).setPreferredWidth(220);
    table.getColumnModel().getColumn(2).setPreferredWidth(130);
    table.getColumnModel().getColumn(3).setPreferredWidth(180);
    table.getColumnModel().getColumn(4).setPreferredWidth(260);

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
            Component component =
                super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            if (!isSelected) {
              component.setForeground(TEXT);
              component.setBackground(WHITE);
            }
            return component;
          }
        };
    table.setDefaultRenderer(Object.class, renderer);
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

  private JTextField createSearchField() {
    Icon searchIcon = new FlatSVGIcon("icons/tim.svg", 18, 18);
    JTextField field = new IconPlaceholderTextField(SEARCH_PLACEHOLDER, searchIcon, MUTED, TEXT);
    styleField(field);
    return field;
  }

  private void styleField(JTextField field) {
    field.setFont(UiTheme.regular(14));
    field.setForeground(TEXT);
    field.setBackground(FIELD_FILL);
    int leftInset = field instanceof IconPlaceholderTextField ? 48 : 12;
    field.setBorder(BorderFactory.createEmptyBorder(0, leftInset, 0, 12));
    field.setOpaque(false);
  }

  private void styleArea(JTextArea area) {
    area.setFont(UiTheme.regular(14));
    area.setForeground(TEXT);
    area.setLineWrap(true);
    area.setWrapStyleWord(true);
    area.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
    area.setOpaque(false);
  }

  private JLabel label(String text) {
    JLabel label = new JLabel(text);
    label.setForeground(MUTED);
    label.setFont(UiTheme.regular(13));
    return label;
  }

  private JLabel sectionTitle(String text) {
    JLabel label = new JLabel(text);
    label.setForeground(PRIMARY_DARK);
    label.setFont(UiTheme.bold(16));
    return label;
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

  private void showWarning(String message) {
    JOptionPane.showMessageDialog(this, message, "Nhà cung cấp", JOptionPane.WARNING_MESSAGE);
  }

  private void showStatus(String message, boolean error) {
    statusLabel.setForeground(error ? DANGER : SUCCESS);
    statusLabel.setText(message);
  }

  private String unwrapMessage(Exception ex) {
    Throwable current = ex;
    while (current.getCause() != null) {
      current = current.getCause();
    }
    return current.getMessage() == null ? "Không xử lý được yêu cầu" : current.getMessage();
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

  private static class OutlinedInputPanel extends JPanel {
    OutlinedInputPanel() {
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
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
      g2.setColor(BORDER);
      g2.setStroke(new BasicStroke(1.2f));
      g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);
      g2.dispose();
    }
  }

  private static class IconPlaceholderTextField extends JTextField {
    private final String placeholder;
    private final Icon icon;
    private final Color placeholderColor;
    private final Color textColor;

    IconPlaceholderTextField(
        String placeholder, Icon icon, Color placeholderColor, Color textColor) {
      this.placeholder = placeholder;
      this.icon = icon;
      this.placeholderColor = placeholderColor;
      this.textColor = textColor;
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
        int iconY = (getHeight() - icon.getIconHeight()) / 2;
        icon.paintIcon(this, g2, 12, iconY);
      }

      if (getText().isEmpty()) {
        g2.setColor(placeholderColor);
        g2.setFont(getFont());
        int textY = getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 2;
        g2.drawString(placeholder, 48, textY);
      } else {
        setForeground(textColor);
      }
      g2.dispose();
    }
  }
}
