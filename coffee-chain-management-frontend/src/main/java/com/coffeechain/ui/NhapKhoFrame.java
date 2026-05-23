package com.coffeechain.ui;

import com.coffeechain.service.InventoryApiClient;
import com.coffeechain.service.InventoryApiClient.CreateImportReceiptItemRequest;
import com.coffeechain.service.InventoryApiClient.CreateImportReceiptRequest;
import com.coffeechain.service.InventoryApiClient.ImportReceiptDto;
import com.coffeechain.service.InventoryApiClient.InventoryLookupDto;
import com.coffeechain.service.InventoryApiClient.OptionDto;
import com.coffeechain.service.SessionManager;
import com.coffeechain.ui.common.RoundedButton;
import com.coffeechain.ui.common.RoundedPanel;
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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
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
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableModel;

/**
 * Man hinh tao phieu nhap kho.
 *
 * <p>Bo cuc bam theo file SVG: header, card thong tin, tim nguyen lieu, 2 bang, hang nhap nhanh,
 * ghi chu va cum nut luu/huy.
 */
public class NhapKhoFrame extends JFrame {

  private static final int ROOT_W = 1440;
  private static final int ROOT_H = 780;
  private static final String EXPIRY_PLACEHOLDER = "yyyy-mm-dd";
  private static final String SEARCH_PLACEHOLDER = "Tìm nguyên liệu";

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

  private final InventoryApiClient apiClient = new InventoryApiClient();
  private final JPanel root = new JPanel(null);

  private final JComboBox<OptionDto> warehouseCombo = new JComboBox<>();
  private final JComboBox<OptionDto> supplierCombo = new JComboBox<>();
  private final JComboBox<OptionDto> ingredientCombo = new JComboBox<>();
  private final JTextField createdDateField = new JTextField(LocalDate.now().toString());
  private final JTextField creatorField =
      new JTextField(SessionManager.getCurrentUserDisplayName());
  private final JTextField quantityField = new JTextField();
  private final JTextField priceField = new JTextField();
  private final JTextField lotField = new JTextField();
  private final JTextField expiryField = new JTextField();
  private final JTextArea noteArea = new JTextArea();
  private final JLabel totalLabel = new JLabel("0");
  private final JLabel statusLabel = new JLabel("Dang tai du lieu...");
  private final RoundedButton saveButton = primaryButton("Lưu phiếu");

  private final DefaultTableModel ingredientTableModel =
      new DefaultTableModel(new Object[] {"Mã NL", "Tên nguyên liệu", "DVT"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return false;
        }
      };
  private final JTable ingredientTable = new JTable(ingredientTableModel);

  private final DefaultTableModel importTableModel =
      new DefaultTableModel(
          new Object[] {
            "Mã NL", "Tên nguyên liệu", "Hạn sử dụng", "Số lượng", "Đơn giá", "Thành tiền"
          },
          0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return false;
        }
      };
  private final JTable importTable = new JTable(importTableModel);

  private List<OptionDto> allIngredients = new ArrayList<>();
  private final List<ImportLine> lines = new ArrayList<>();
  private final DecimalFormat numberFormat = new DecimalFormat("#,##0.###");

  public NhapKhoFrame() {
    setTitle("Phụng Lộc - Nhập kho");
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setResizable(false);

    root.setPreferredSize(new Dimension(ROOT_W, ROOT_H));
    root.setBackground(WHITE);
    setContentPane(root);

    buildHeader();
    buildReceiptInfo();
    buildIngredientPicker();
    buildTables();
    buildQuickInputRow();
    buildNoteAndFooter();
    bindAmountPreview();
    loadLookups();

    pack();
    setLocationRelativeTo(null);
  }

  private void buildHeader() {
    JLabel title = new JLabel("NHẬP KHO");
    title.setBounds(44, 22, 420, 40);
    title.setForeground(PRIMARY);
    title.setFont(UiTheme.bold(32));
    root.add(title);

    RoundedButton backButton = primaryButton("Quay lại");

    FlatSVGIcon backIcon = new FlatSVGIcon("icons/nhap-kho/left.svg", 16, 18);
    backButton.setIcon(backIcon);
    backButton.setIconTextGap(8);
    backButton.setHorizontalAlignment(SwingConstants.CENTER);

    backButton.setBounds(1250, 30, 110, 34);
    backButton.addActionListener(
        e -> {
          new KhoMenuFrame().setVisible(true);
          dispose();
        });

    root.add(backButton);
  }

  private void buildReceiptInfo() {
    JLabel filterTitle = new JLabel("Phiếu nhập");
    filterTitle.setBounds(44, 76, 200, 24);
    filterTitle.setForeground(TEXT);
    filterTitle.setFont(UiTheme.bold(16));
    root.add(filterTitle);

    RoundedPanel card = new RoundedPanel(16, WHITE, SOFT_BORDER);
    card.setLayout(null);
    card.setBounds(44, 104, 1352, 78);
    root.add(card);

    addLabel(card, "Kho nhận:", 20, 10, 100, 18);
    addCombo(card, warehouseCombo, 20, 32, 250, 34);

    addLabel(card, "Nhà cung cấp:", 310, 10, 120, 18);
    addCombo(card, supplierCombo, 310, 32, 250, 34);

    addLabel(card, "Ngày nhập:", 600, 10, 100, 18);
    addField(card, createdDateField, 600, 32, 220, 34, false, LocalDate.now().toString());

    addLabel(card, "Người tạo:", 860, 10, 100, 18);
    addField(
        card, creatorField, 860, 32, 220, 34, false, SessionManager.getCurrentUserDisplayName());

    //        statusLabel.setBounds(1060, 18, 290, 44);
    //        statusLabel.setForeground(MUTED);
    //        statusLabel.setFont(UiTheme.regular(12));
    //        statusLabel.setVerticalAlignment(SwingConstants.CENTER);
    //        card.add(statusLabel);
  }

  private void buildIngredientPicker() {
    JTextField searchField = createSearchField();
    OutlinedInputPanel searchPanel = new OutlinedInputPanel();
    searchPanel.setLayout(new BorderLayout());
    searchPanel.setBounds(44, 218, 460, 36);
    searchPanel.add(searchField, BorderLayout.CENTER);
    root.add(searchPanel);

    RoundedButton searchButton = primaryButton("Tìm");
    searchButton.setBounds(518, 218, 60, 36);
    root.add(searchButton);

    Runnable doSearch = () -> filterIngredientTable(searchField.getText());
    searchButton.addActionListener(e -> doSearch.run());
    searchField
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {
              @Override
              public void insertUpdate(DocumentEvent e) {
                doSearch.run();
              }

              @Override
              public void removeUpdate(DocumentEvent e) {
                doSearch.run();
              }

              @Override
              public void changedUpdate(DocumentEvent e) {
                doSearch.run();
              }
            });
  }

  private void buildTables() {
    JLabel leftTitle = new JLabel("DANH SÁCH NGUYÊN LIỆU");
    leftTitle.setBounds(44, 262, 400, 24);
    leftTitle.setForeground(TEXT);
    leftTitle.setFont(UiTheme.bold(14));
    root.add(leftTitle);

    configureTable(ingredientTable);
    ingredientTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    ingredientTable
        .getSelectionModel()
        .addListSelectionListener(
            e -> {
              if (!e.getValueIsAdjusting()) {
                fillSelectedIngredientToDetail();
              }
            });

    JScrollPane leftScroll = hiddenScrollPane(ingredientTable);
    leftScroll.setBounds(44, 292, 420, 190);
    root.add(leftScroll);

    JLabel rightTitle = new JLabel("NGUYÊN LIỆU SẼ NHẬP");
    rightTitle.setBounds(540, 262, 400, 24);
    rightTitle.setForeground(TEXT);
    rightTitle.setFont(UiTheme.bold(14));
    root.add(rightTitle);

    configureTable(importTable);
    importTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    importTable.addMouseListener(
        new java.awt.event.MouseAdapter() {
          @Override
          public void mouseClicked(java.awt.event.MouseEvent e) {
            if (e.getClickCount() == 2) {
              removeSelectedImportLine();
            }
          }
        });

    JScrollPane rightScroll = hiddenScrollPane(importTable);
    rightScroll.setBounds(540, 292, 856, 190);
    root.add(rightScroll);
  }

  private void buildQuickInputRow() {
    JLabel detailTitle = new JLabel("CHI TIẾT DÒNG NHẬP");
    detailTitle.setBounds(44, 496, 400, 24);
    detailTitle.setForeground(TEXT);
    detailTitle.setFont(UiTheme.bold(14));
    root.add(detailTitle);

    RoundedPanel detailCard = new RoundedPanel(12, WHITE, SOFT_BORDER);
    detailCard.setBounds(44, 526, 1352, 104);
    detailCard.setLayout(null);
    root.add(detailCard);

    addLabel(detailCard, "Nguyên liệu:", 20, 12, 120, 18);
    addCombo(detailCard, ingredientCombo, 20, 34, 250, 30);

    addLabel(detailCard, "Số lượng nhập:", 330, 12, 120, 18);
    addField(detailCard, quantityField, 330, 34, 140, 30, true, "0");

    addLabel(detailCard, "Đơn giá nhập:", 530, 12, 120, 18);
    addField(detailCard, priceField, 530, 34, 140, 30, true, "0");

    addLabel(detailCard, "Số lô:", 730, 12, 80, 18);
    addField(detailCard, lotField, 730, 34, 140, 30, true, "LOT-001");

    addLabel(detailCard, "Hạn sử dụng:", 930, 12, 120, 18);
    addField(
        detailCard, expiryField, 930, 34, 140, 30, true, EXPIRY_PLACEHOLDER, new ClockLineIcon());
    installPlaceholder(expiryField, EXPIRY_PLACEHOLDER);

    RoundedButton addToListButton = primaryButton("Thêm vào phiếu");
    addToListButton.setBounds(1130, 34, 150, 30);
    addToListButton.addActionListener(e -> addLine());
    detailCard.add(addToListButton);
  }

  private void buildNoteAndFooter() {
    JLabel noteLabel = new JLabel("Ghi chú phiếu");
    noteLabel.setBounds(44, 646, 240, 20);
    noteLabel.setForeground(UiTheme.TEXT_DARK);
    noteLabel.setFont(UiTheme.regular(12));
    root.add(noteLabel);

    RoundedPanel notePanel = new RoundedPanel(12, WHITE, SOFT_BORDER);
    notePanel.setBounds(44, 670, 904, 62);
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

    JLabel totalTextLabel = new JLabel("Tổng tiền:");
    totalTextLabel.setBounds(1030, 670, 120, 28);
    totalTextLabel.setForeground(MUTED);
    totalTextLabel.setFont(UiTheme.regular(14));
    root.add(totalTextLabel);

    totalLabel.setBounds(1120, 670, 260, 28);
    totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    totalLabel.setForeground(PRIMARY_DARK);
    totalLabel.setFont(UiTheme.bold(22));
    root.add(totalLabel);

    saveButton.setBounds(1030, 710, 125, 34);
    saveButton.addActionListener(e -> saveReceipt());
    root.add(saveButton);

    RoundedButton cancelButton = secondaryButton("Hủy");
    cancelButton.setBounds(1179, 710, 96, 34);
    cancelButton.addActionListener(
        e -> {
          new KhoMenuFrame().setVisible(true);
          dispose();
        });
    root.add(cancelButton);
  }

  private void loadLookups() {
    setFormEnabled(false);
    statusLabel.setText("Dang tai du lieu...");
    new SwingWorker<InventoryLookupDto, Void>() {
      @Override
      protected InventoryLookupDto doInBackground() throws Exception {
        return apiClient.getImportLookups();
      }

      @Override
      protected void done() {
        try {
          InventoryLookupDto lookup = get();
          warehouseCombo.setModel(
              new DefaultComboBoxModel<>(lookup.getWarehouses().toArray(new OptionDto[0])));
          supplierCombo.setModel(
              new DefaultComboBoxModel<>(lookup.getSuppliers().toArray(new OptionDto[0])));
          allIngredients = lookup.getIngredients();
          ingredientCombo.setModel(
              new DefaultComboBoxModel<>(allIngredients.toArray(new OptionDto[0])));
          populateIngredientTable(allIngredients);
          statusLabel.setText("Da tai du lieu. San sang tao phieu nhap.");
          setFormEnabled(true);
        } catch (Exception ex) {
          statusLabel.setText("Khong tai duoc du lieu nhap kho");
          JOptionPane.showMessageDialog(
              thisFrame(), unwrapMessage(ex), "Loi", JOptionPane.ERROR_MESSAGE);
        }
      }
    }.execute();
  }

  private void addLine() {
    OptionDto ingredient = (OptionDto) ingredientCombo.getSelectedItem();
    if (ingredient == null) {
      showWarning("Vui long chon nguyen lieu");
      return;
    }

    BigDecimal quantity =
        parsePositiveDecimal(quantityField.getText(), "So luong nhap phai lon hon 0");
    if (quantity == null) {
      return;
    }

    BigDecimal price = parseNonNegativeDecimal(priceField.getText(), "Don gia nhap khong hop le");
    if (price == null) {
      return;
    }

    String expiryText = expiryField.getText();
    LocalDate expiry = parseExpiryDate(expiryText);
    if (!isBlankOrExpiryPlaceholder(expiryText) && expiry == null) {
      return;
    }

    String lotNo = lotField.getText() == null ? "" : lotField.getText().trim();
    ImportLine line = new ImportLine(ingredient, quantity, price, lotNo, expiry);
    lines.add(line);

    importTableModel.addRow(
        new Object[] {
          ingredient.getId(),
          ingredient.getName(),
          expiry == null ? "" : expiry.toString(),
          numberFormat.format(quantity),
          numberFormat.format(price),
          numberFormat.format(line.amount())
        });

    updateTotal();
    clearLineInputs();
  }

  private void removeSelectedImportLine() {
    int selectedRow = importTable.getSelectedRow();
    if (selectedRow < 0) {
      return;
    }

    int confirm =
        JOptionPane.showConfirmDialog(
            this,
            "Ban muon xoa dong nguyen lieu nay khoi phieu nhap?",
            "Xoa dong nhap",
            JOptionPane.YES_NO_OPTION);
    if (confirm != JOptionPane.YES_OPTION) {
      return;
    }

    lines.remove(selectedRow);
    importTableModel.removeRow(selectedRow);
    updateTotal();
  }

  private void saveReceipt() {
    OptionDto warehouse = (OptionDto) warehouseCombo.getSelectedItem();
    OptionDto supplier = (OptionDto) supplierCombo.getSelectedItem();
    if (warehouse == null || supplier == null) {
      showWarning("Vui long chon kho va nha cung cap");
      return;
    }
    if (lines.isEmpty()) {
      showWarning("Vui long them it nhat mot dong nguyen lieu");
      return;
    }

    CreateImportReceiptRequest request = new CreateImportReceiptRequest();
    request.setMaKho(warehouse.getId());
    request.setMaNhaCungCap(supplier.getId());
    request.setGhiChu(noteArea.getText().trim());

    List<CreateImportReceiptItemRequest> items = new ArrayList<>();
    for (ImportLine line : lines) {
      CreateImportReceiptItemRequest item = new CreateImportReceiptItemRequest();
      item.setMaNguyenLieu(line.ingredient().getId());
      item.setSoLuongNhap(line.quantity());
      item.setDonGiaNhap(line.price());
      item.setSoLo(line.lotNo());
      item.setHanSuDung(line.expiryDate());
      items.add(item);
    }
    request.setItems(items);

    saveButton.setEnabled(false);
    statusLabel.setText("Dang luu phieu nhap...");
    new SwingWorker<ImportReceiptDto, Void>() {
      @Override
      protected ImportReceiptDto doInBackground() throws Exception {
        return apiClient.createImportReceipt(request);
      }

      @Override
      protected void done() {
        saveButton.setEnabled(true);
        try {
          ImportReceiptDto receipt = get();
          statusLabel.setText("Da luu phieu nhap #" + receipt.getMaPhieuNhap());
          JOptionPane.showMessageDialog(
              thisFrame(),
              "Da tao phieu nhap #"
                  + receipt.getMaPhieuNhap()
                  + "\nKho: "
                  + receipt.getTenKho()
                  + "\nNCC: "
                  + receipt.getTenNhaCungCap()
                  + "\nTong tien: "
                  + numberFormat.format(receipt.getTongTien()),
              "Nhap kho",
              JOptionPane.INFORMATION_MESSAGE);
          resetReceipt();
        } catch (Exception ex) {
          statusLabel.setText("Luu phieu nhap that bai");
          JOptionPane.showMessageDialog(
              thisFrame(), unwrapMessage(ex), "Loi", JOptionPane.ERROR_MESSAGE);
        }
      }
    }.execute();
  }

  private void bindAmountPreview() {
    DocumentListener listener =
        new DocumentListener() {
          @Override
          public void insertUpdate(DocumentEvent e) {
            updateAmountPreview();
          }

          @Override
          public void removeUpdate(DocumentEvent e) {
            updateAmountPreview();
          }

          @Override
          public void changedUpdate(DocumentEvent e) {
            updateAmountPreview();
          }
        };
    quantityField.getDocument().addDocumentListener(listener);
    priceField.getDocument().addDocumentListener(listener);
  }

  private void updateAmountPreview() {
    // Tong tien phieu duoc cap nhat sau khi them dong vao bang nhap.
  }

  private void resetReceipt() {
    lines.clear();
    importTableModel.setRowCount(0);
    noteArea.setText("");
    clearLineInputs();
    updateTotal();
  }

  private void updateTotal() {
    BigDecimal total = BigDecimal.ZERO;
    for (ImportLine line : lines) {
      total = total.add(line.amount());
    }
    totalLabel.setText(numberFormat.format(total));
  }

  private void clearLineInputs() {
    quantityField.setText("");
    priceField.setText("");
    lotField.setText("");
    expiryField.setText(EXPIRY_PLACEHOLDER);
    expiryField.setForeground(MUTED);
    SwingUtilities.invokeLater(() -> ingredientCombo.requestFocusInWindow());
  }

  private BigDecimal parsePositiveDecimal(String raw, String message) {
    BigDecimal value = parseDecimal(raw, message);
    if (value == null) {
      return null;
    }
    if (value.compareTo(BigDecimal.ZERO) <= 0) {
      showWarning(message);
      return null;
    }
    return value;
  }

  private BigDecimal parseNonNegativeDecimal(String raw, String message) {
    BigDecimal value = parseDecimal(raw, message);
    if (value == null) {
      return null;
    }
    if (value.compareTo(BigDecimal.ZERO) < 0) {
      showWarning(message);
      return null;
    }
    return value;
  }

  private BigDecimal parseDecimal(String raw, String message) {
    if (raw == null || raw.isBlank()) {
      showWarning(message);
      return null;
    }
    try {
      return new BigDecimal(raw.trim().replace(",", ""));
    } catch (NumberFormatException ex) {
      showWarning(message);
      return null;
    }
  }

  private LocalDate parseExpiryDate(String raw) {
    if (isBlankOrExpiryPlaceholder(raw)) {
      return null;
    }
    try {
      return LocalDate.parse(raw.trim());
    } catch (DateTimeParseException ex) {
      showWarning("Han su dung phai co dang yyyy-MM-dd, vi du 2027-12-31");
      return null;
    }
  }

  private boolean isBlankOrExpiryPlaceholder(String raw) {
    if (raw == null || raw.isBlank()) {
      return true;
    }
    String text = raw.trim();
    return text.equalsIgnoreCase("yyyy-mm-dd") || text.equalsIgnoreCase("yyyy-MM-dd");
  }

  private void setFormEnabled(boolean enabled) {
    warehouseCombo.setEnabled(enabled);
    supplierCombo.setEnabled(enabled);
    ingredientCombo.setEnabled(enabled);
    quantityField.setEnabled(enabled);
    priceField.setEnabled(enabled);
    lotField.setEnabled(enabled);
    expiryField.setEnabled(enabled);
    noteArea.setEnabled(enabled);
    saveButton.setEnabled(enabled);
  }

  private void populateIngredientTable(List<OptionDto> ingredients) {
    ingredientTableModel.setRowCount(0);
    for (OptionDto ingredient : ingredients) {
      ingredientTableModel.addRow(
          new Object[] {ingredient.getId(), ingredient.getName(), getUnitText(ingredient)});
    }
  }

  private void filterIngredientTable(String keyword) {
    if (allIngredients == null) {
      return;
    }

    String text = keyword == null ? "" : keyword.trim().toLowerCase();
    if (text.equals(SEARCH_PLACEHOLDER.toLowerCase())) {
      text = "";
    }
    if (text.isEmpty()) {
      populateIngredientTable(allIngredients);
      return;
    }

    List<OptionDto> filtered = new ArrayList<>();
    for (OptionDto ingredient : allIngredients) {
      String id = String.valueOf(ingredient.getId()).toLowerCase();
      String name = ingredient.getName() == null ? "" : ingredient.getName().toLowerCase();
      if (id.contains(text) || name.contains(text)) {
        filtered.add(ingredient);
      }
    }
    populateIngredientTable(filtered);
  }

  private void fillSelectedIngredientToDetail() {
    int selectedRow = ingredientTable.getSelectedRow();
    if (selectedRow < 0) {
      return;
    }

    Long ingredientId = Long.valueOf(ingredientTable.getValueAt(selectedRow, 0).toString());
    for (int i = 0; i < ingredientCombo.getItemCount(); i++) {
      OptionDto item = ingredientCombo.getItemAt(i);
      if (item != null && item.getId().equals(ingredientId)) {
        ingredientCombo.setSelectedIndex(i);
        quantityField.requestFocusInWindow();
        return;
      }
    }
  }

  private String getUnitText(OptionDto ingredient) {
    String unit = ingredient.getDescription();
    return unit == null || unit.isBlank() ? "-" : unit;
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
  }

  private void addCombo(JPanel parent, JComboBox<?> combo, int x, int y, int w, int h) {
    styleCombo(combo);
    OutlinedInputPanel panel = new OutlinedInputPanel();
    panel.setLayout(new BorderLayout());
    panel.setBounds(x, y, w, h);
    panel.add(combo, BorderLayout.CENTER);
    parent.add(panel);
  }

  private void addField(
      JPanel parent,
      JTextField field,
      int x,
      int y,
      int w,
      int h,
      boolean editable,
      String tooltip) {
    addField(parent, field, x, y, w, h, editable, tooltip, null);
  }

  private void addField(
      JPanel parent,
      JTextField field,
      int x,
      int y,
      int w,
      int h,
      boolean editable,
      String tooltip,
      Icon trailingIcon) {
    styleField(field, tooltip);
    field.setEditable(editable);
    field.setFocusable(editable);
    OutlinedInputPanel panel = new OutlinedInputPanel();
    panel.setLayout(new BorderLayout());
    panel.setBounds(x, y, w, h);
    panel.add(field, BorderLayout.CENTER);
    if (trailingIcon != null) {
      JLabel iconLabel = new JLabel(trailingIcon);
      iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
      iconLabel.setPreferredSize(new Dimension(32, 1));
      panel.add(iconLabel, BorderLayout.EAST);
    }
    parent.add(panel);
  }

  private void addLabel(JPanel parent, String text, int x, int y, int w, int h) {
    JLabel label = new JLabel(text);
    label.setBounds(x, y, w, h);
    label.setForeground(MUTED);
    label.setFont(UiTheme.regular(13));
    parent.add(label);
  }

  private JTextField createSearchField() {
    Icon searchIcon = new FlatSVGIcon("icons/tim.svg", 18, 18);
    JTextField field = new IconPlaceholderTextField(SEARCH_PLACEHOLDER, searchIcon, MUTED, TEXT);
    styleField(field, SEARCH_PLACEHOLDER);
    return field;
  }

  private void installPlaceholder(JTextField field, String placeholder) {
    field.setText(placeholder);
    field.setForeground(MUTED);
    field.addFocusListener(
        new java.awt.event.FocusAdapter() {
          @Override
          public void focusGained(java.awt.event.FocusEvent e) {
            if (field.getText().equals(placeholder)) {
              field.setText("");
              field.setForeground(TEXT);
            }
          }

          @Override
          public void focusLost(java.awt.event.FocusEvent e) {
            if (field.getText().isBlank()) {
              field.setText(placeholder);
              field.setForeground(MUTED);
            }
          }
        });
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
    RoundedButton button =
        new RoundedButton(text)
            .background(Color.decode("#B9B9B9"))
            .hover(Color.decode("#A8A8A8"))
            .radius(10);
    button.setForeground(TEXT);
    return button;
  }

  private void showWarning(String message) {
    JOptionPane.showMessageDialog(this, message, "Nhap kho", JOptionPane.WARNING_MESSAGE);
  }

  private JFrame thisFrame() {
    return this;
  }

  private String unwrapMessage(Exception ex) {
    Throwable current = ex;
    while (current.getCause() != null) {
      current = current.getCause();
    }
    return current.getMessage() == null ? "Khong xu ly duoc yeu cau" : current.getMessage();
  }

  private record ImportLine(
      OptionDto ingredient,
      BigDecimal quantity,
      BigDecimal price,
      String lotNo,
      LocalDate expiryDate) {

    BigDecimal amount() {
      return quantity.multiply(price);
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
      // OutlinedInputPanel da ve nen trang va vien theo mockup.
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

  private static class ClockLineIcon implements Icon {

    private static final int SIZE = 18;

    @Override
    public int getIconWidth() {
      return SIZE;
    }

    @Override
    public int getIconHeight() {
      return SIZE;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
      g2.setColor(MUTED);
      g2.drawOval(x + 2, y + 2, SIZE - 4, SIZE - 4);
      int cx = x + SIZE / 2;
      int cy = y + SIZE / 2;
      g2.drawLine(cx, cy, cx, y + 5);
      g2.drawLine(cx, cy, x + 13, y + 12);
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
