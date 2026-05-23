package com.coffeechain.ui;

import com.coffeechain.service.SessionManager;
import com.coffeechain.service.StocktakeApiClient;
import com.coffeechain.service.StocktakeApiClient.OptionDto;
import com.coffeechain.service.StocktakeApiClient.StocktakeDto;
import com.coffeechain.service.StocktakeApiClient.StocktakeItemRequest;
import com.coffeechain.service.StocktakeApiClient.StocktakeLookupDto;
import com.coffeechain.service.StocktakeApiClient.StocktakeRequest;
import com.coffeechain.service.StocktakeApiClient.StocktakeSystemStockDto;
import com.coffeechain.ui.common.IconLoader;
import com.coffeechain.ui.common.PermissionUtil;
import com.coffeechain.ui.common.RoundedButton;
import com.coffeechain.ui.common.RoundedPanel;
import com.coffeechain.ui.common.UiTheme;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class KiemKhoFrame extends JFrame {
  private static final int ROOT_W = 1440;
  private static final int ROOT_H = 920;
  private static final int VIEW_H = 790;
  private static final Color WHITE = Color.WHITE;
  private static final Color PRIMARY = UiTheme.PRIMARY;
  private static final Color TEXT = UiTheme.TEXT_DARK;
  private static final Color MUTED = UiTheme.TEXT_MUTED;
  private static final Color BORDER = Color.decode("#B9B9B9");
  private static final Color SOFT_BORDER = Color.decode("#E6E6E6");
  private static final Color TABLE_HEAD = Color.decode("#D9D9D9");
  private static final Color DANGER = Color.decode("#BE3C2D");
  private static final Color SUCCESS = Color.decode("#248A52");
  private static final Color FIELD_FILL = Color.WHITE;
  private static final Color FIELD_BORDER = Color.decode("#B9B9B9");

  private final JPanel root = new JPanel(null);
  private final StocktakeApiClient apiClient = new StocktakeApiClient();
  private final DecimalFormat numberFormat = new DecimalFormat("#,##0.###");

  private final JComboBox<OptionDto> warehouseCombo = new JComboBox<>();
  private final JComboBox<OptionDto> ingredientCombo = new JComboBox<>();
  private final JComboBox<OptionDto> statusCombo = new JComboBox<>();
  private final JComboBox<StocktakeSystemStockDto> lotCombo = new JComboBox<>();
  private final JComboBox<OptionDto> handlingCombo = new JComboBox<>();

  private final JTextField dateField = new JTextField(LocalDate.now().toString());
  private final JTextField inspectorField =
      new JTextField(SessionManager.getCurrentUserDisplayName());
  private final JTextField historyDateField = new JTextField();
  private final JTextField systemQtyField = new JTextField();
  private final JTextField actualQtyField = new JTextField();
  private final JTextField diffQtyField = new JTextField();
  private final JTextField expiryField = new JTextField();
  private final JTextField reasonField = new JTextField();
  private final JTextArea noteArea = new JTextArea();
  private final JLabel statusLabel = new JLabel("Đang tải dữ liệu...");
  private final RoundedButton openDraftButton = secondaryButton("Mở phiếu");
  private final RoundedButton cancelDraftButton = secondaryButton("Hủy phiếu");
  private final RoundedButton saveDraftButton = primaryButton("Lưu nháp");
  private final RoundedButton completeButton = primaryButton("Hoàn tất");

  private final DefaultTableModel stockModel =
      new DefaultTableModel(
          new Object[] {"Mã lô", "Nguyên liệu", "DVT", "Hệ thống", "HSD", "Trạng thái"}, 0) {
        public boolean isCellEditable(int r, int c) {
          return false;
        }
      };
  private final JTable stockTable = new JTable(stockModel);
  private final DefaultTableModel historyModel =
      new DefaultTableModel(
          new Object[] {"Mã phiếu", "Ngày", "Kho", "Trạng thái", "Dòng", "Người kiểm", "Ghi chú"},
          0) {
        public boolean isCellEditable(int r, int c) {
          return false;
        }
      };
  private final JTable historyTable = new JTable(historyModel);
  private final DefaultTableModel itemModel =
      new DefaultTableModel(
          new Object[] {
            "Lô", "Nguyên liệu", "DVT", "Hệ thống", "Thực tế", "Chênh lệch", "Xử lý", "Lý do"
          },
          0) {
        public boolean isCellEditable(int r, int c) {
          return false;
        }
      };
  private final JTable itemTable = new JTable(itemModel);

  private final List<StocktakeLine> draftLines = new ArrayList<>();
  private boolean loading;
  private boolean suppressEvents;
  private boolean formActionsEnabled;
  private Long editingStocktakeId;

  public KiemKhoFrame() {
    setTitle("Phụng Lộc - Kiểm kho");
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setResizable(false);
    root.setPreferredSize(new Dimension(ROOT_W, ROOT_H));
    root.setBackground(WHITE);
    setContentPane(createHiddenPageScrollPane(root));
    buildHeader();
    buildFilterCard();
    buildTables();
    buildDetailCard();
    buildItemTable();
    buildFooter();
    bindEvents();
    loadLookups();
    pack();
    setLocationRelativeTo(null);
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

  private void buildHeader() {
    JLabel title = new JLabel("KIỂM KHO");
    title.setBounds(44, 22, 520, 40);
    title.setForeground(PRIMARY);
    title.setFont(UiTheme.bold(32));
    root.add(title);
    RoundedButton backButton = primaryButton("Quay lại");
    backButton.setIcon(IconLoader.svg("icons/nhap-kho/left.svg", 16, 18));
    backButton.setIconTextGap(8);
    backButton.setBounds(1250, 30, 110, 34);
    backButton.addActionListener(
        e -> {
          new KhoMenuFrame().setVisible(true);
          dispose();
        });
    root.add(backButton);
  }

  private void buildFilterCard() {
    JLabel title = sectionTitle("Thông tin kiểm kho", 44, 76);
    root.add(title);
    RoundedPanel card = card(44, 104, 1352, 90);
    root.add(card);
    addLabel(card, "Kho kiểm:", 20, 12, 120, 18);
    addCombo(card, warehouseCombo, 20, 34, 300, 34);
    warehouseCombo.setRenderer(new NameOnlyComboRenderer());
    addLabel(card, "Nguyên liệu:", 360, 12, 120, 18);
    addCombo(card, ingredientCombo, 360, 34, 300, 34);
    addLabel(card, "Ngày kiểm:", 700, 12, 120, 18);
    addField(card, dateField, 700, 34, 170, 34, false);
    addLabel(card, "Người kiểm:", 910, 12, 120, 18);
    addField(card, inspectorField, 910, 34, 190, 34, false);
    RoundedButton reloadButton = secondaryButton("Tải lại");
    reloadButton.setBounds(1230, 34, 96, 34);
    reloadButton.addActionListener(e -> loadSystemStockAndHistory());
    card.add(reloadButton);
  }

  private void buildTables() {
    root.add(sectionTitle("TỒN HỆ THỐNG THEO LÔ", 44, 216));
    configureTable(stockTable, -1);
    stockTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    stockTable
        .getSelectionModel()
        .addListSelectionListener(
            e -> {
              if (!e.getValueIsAdjusting()) fillSelectedStockToForm();
            });
    JScrollPane stockScroll = hiddenScrollPane(stockTable);
    stockScroll.setBounds(44, 246, 600, 218);
    root.add(stockScroll);

    root.add(sectionTitle("PHIẾU KIỂM KHO GẦN ĐÂY", 690, 216));
    addLabel(root, "Ngày kiểm phiếu:", 690, 244, 100, 18);
    addField(root, historyDateField, 690, 266, 135, 32, true);
    historyDateField.setToolTipText("yyyy-MM-dd");
    addLabel(root, "Trạng thái phiếu:", 845, 244, 130, 18);
    addCombo(root, statusCombo, 845, 266, 230, 32);
    statusCombo.setRenderer(new NameOnlyComboRenderer());
    openDraftButton.setBounds(1100, 266, 100, 32);
    openDraftButton.addActionListener(e -> openSelectedStocktake());
    root.add(openDraftButton);
    cancelDraftButton.setBounds(1215, 266, 110, 32);
    cancelDraftButton.addActionListener(e -> cancelSelectedStocktake());
    root.add(cancelDraftButton);
    configureTable(historyTable, 3);
    historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    historyTable
        .getSelectionModel()
        .addListSelectionListener(
            e -> {
              if (!e.getValueIsAdjusting()) {
                updateActionButtons();
              }
            });
    JScrollPane historyScroll = hiddenScrollPane(historyTable);
    historyScroll.setBounds(690, 310, 706, 154);
    root.add(historyScroll);
  }

  private void buildDetailCard() {
    root.add(sectionTitle("CHI TIẾT KIỂM KHO", 44, 480));
    RoundedPanel card = card(44, 510, 1352, 96);
    root.add(card);
    addLabel(card, "Lô hàng:", 20, 12, 120, 18);
    addCombo(card, lotCombo, 20, 34, 210, 30);
    lotCombo.setRenderer(new LotComboRenderer());
    addLabel(card, "Hệ thống:", 260, 12, 120, 18);
    addField(card, systemQtyField, 260, 34, 130, 30, false);
    addLabel(card, "Thực tế:", 420, 12, 120, 18);
    addField(card, actualQtyField, 420, 34, 130, 30, true);
    addLabel(card, "Chênh lệch:", 580, 12, 120, 18);
    addField(card, diffQtyField, 580, 34, 130, 30, false);
    addLabel(card, "Hạn sử dụng:", 740, 12, 120, 18);
    addField(card, expiryField, 740, 34, 130, 30, false);
    addLabel(card, "Hướng xử lý:", 900, 12, 120, 18);
    addCombo(card, handlingCombo, 900, 34, 190, 30);
    addLabel(card, "Lý do:", 1120, 12, 100, 18);
    addField(card, reasonField, 1120, 34, 130, 30, true);
    RoundedButton addButton = primaryButton("Thêm");
    addButton.setBounds(1270, 34, 62, 30);
    addButton.addActionListener(e -> addOrUpdateLine());
    card.add(addButton);
  }

  private void buildItemTable() {
    root.add(sectionTitle("DÒNG KIỂM KHO TRONG PHIẾU", 44, 620));
    configureTable(itemTable, 5);
    itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    itemTable
        .getSelectionModel()
        .addListSelectionListener(
            e -> {
              if (!e.getValueIsAdjusting()) fillSelectedDraftLine();
            });
    JScrollPane itemScroll = hiddenScrollPane(itemTable);
    itemScroll.setBounds(44, 650, 1352, 170);
    root.add(itemScroll);
  }

  private void buildFooter() {
    JLabel noteLabel = new JLabel("Ghi chú kiểm kho");
    noteLabel.setBounds(44, 828, 220, 18);
    noteLabel.setForeground(TEXT);
    noteLabel.setFont(UiTheme.regular(12));
    root.add(noteLabel);
    RoundedPanel notePanel = card(44, 848, 760, 32);
    root.add(notePanel);
    noteArea.setFont(UiTheme.regular(13));
    noteArea.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
    JScrollPane noteScroll = new JScrollPane(noteArea);
    noteScroll.setBounds(1, 1, 758, 30);
    noteScroll.setBorder(null);
    hideScrollBarsButKeepWheel(noteScroll);
    notePanel.add(noteScroll);
    RoundedButton clearButton = secondaryButton("Xóa dòng");
    clearButton.setBounds(850, 848, 110, 32);
    clearButton.addActionListener(e -> removeSelectedLine());
    root.add(clearButton);
    RoundedButton resetButton = secondaryButton("Hủy nhập");
    resetButton.setBounds(980, 848, 110, 32);
    resetButton.addActionListener(e -> resetDraft());
    root.add(resetButton);
    saveDraftButton.setBounds(1110, 848, 110, 32);
    saveDraftButton.addActionListener(e -> saveStocktake(false));
    root.add(saveDraftButton);
    completeButton.setBounds(1240, 848, 110, 32);
    completeButton.addActionListener(e -> saveStocktake(true));
    root.add(completeButton);
    statusLabel.setBounds(44, 884, 900, 18);
    statusLabel.setForeground(MUTED);
    statusLabel.setFont(UiTheme.regular(13));
    root.add(statusLabel);
  }

  private void bindEvents() {
    warehouseCombo.addActionListener(
        e -> {
          if (!suppressEvents) loadSystemStockAndHistory();
        });
    ingredientCombo.addActionListener(
        e -> {
          if (!suppressEvents) loadSystemStockAndHistory();
        });
    statusCombo.addActionListener(
        e -> {
          if (!suppressEvents) loadHistoryOnly();
        });
    historyDateField.addActionListener(e -> loadHistoryOnly());
    lotCombo.addActionListener(e -> updateStockFieldsFromCombo());
    actualQtyField
        .getDocument()
        .addDocumentListener(
            new javax.swing.event.DocumentListener() {
              public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateDiffField();
              }

              public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateDiffField();
              }

              public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateDiffField();
              }
            });
  }

  private void loadLookups() {
    setFormEnabled(false);
    loading = true;
    statusLabel.setText("Đang tải dữ liệu kiểm kho...");
    new SwingWorker<StocktakeLookupDto, Void>() {
      protected StocktakeLookupDto doInBackground() throws Exception {
        return apiClient.getLookups();
      }

      protected void done() {
        try {
          StocktakeLookupDto lookup = get();
          suppressEvents = true;
          warehouseCombo.setModel(
              new DefaultComboBoxModel<>(lookup.getWarehouses().toArray(new OptionDto[0])));
          ingredientCombo.setModel(
              new DefaultComboBoxModel<>(
                  withAllOption("Tất cả nguyên liệu", lookup.getIngredients())));
          statusCombo.setModel(
              new DefaultComboBoxModel<>(withAllOption("Tất cả trạng thái", lookup.getStatuses())));
          handlingCombo.setModel(
              new DefaultComboBoxModel<>(lookup.getHandlingOptions().toArray(new OptionDto[0])));
          selectHandling("NO_ACTION");
          suppressEvents = false;
          setFormEnabled(true);
          loading = false;
          loadSystemStockAndHistory();
        } catch (Exception ex) {
          loading = false;
          setFormEnabled(true);
          statusLabel.setText("Không tải được dữ liệu kiểm kho");
          JOptionPane.showMessageDialog(
              KiemKhoFrame.this, unwrapMessage(ex), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
      }
    }.execute();
  }

  private void loadSystemStockAndHistory() {
    if (loading) return;
    OptionDto warehouse = selectedOption(warehouseCombo);
    if (warehouse == null || warehouse.getId() == null) return;
    loading = true;
    statusLabel.setText("Đang tải tồn hệ thống...");
    Long maKho = warehouse.getId();
    Long maNguyenLieu = selectedOptionId(ingredientCombo);
    String trangThai = selectedOptionCode(statusCombo);
    String fromDate;
    String toDate;
    try {
      fromDate = historyDateParam(false);
      toDate = historyDateParam(true);
    } catch (IllegalArgumentException ex) {
      loading = false;
      showWarning(ex.getMessage());
      return;
    }
    new SwingWorker<StocktakeScreenData, Void>() {
      protected StocktakeScreenData doInBackground() throws Exception {
        return new StocktakeScreenData(
            apiClient.getSystemStock(maKho, maNguyenLieu),
            apiClient.searchStocktakes(maKho, trangThai, fromDate, toDate, null));
      }

      protected void done() {
        try {
          StocktakeScreenData data = get();
          populateStockTable(data.stocks());
          lotCombo.setModel(
              new DefaultComboBoxModel<>(data.stocks().toArray(new StocktakeSystemStockDto[0])));
          populateHistoryTable(data.history());
          updateStockFieldsFromCombo();
          statusLabel.setText("Đã tải " + data.stocks().size() + " lô tồn hệ thống.");
        } catch (Exception ex) {
          statusLabel.setText("Không tải được dữ liệu kiểm kho");
          JOptionPane.showMessageDialog(
              KiemKhoFrame.this, unwrapMessage(ex), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } finally {
          loading = false;
        }
      }
    }.execute();
  }

  private void loadHistoryOnly() {
    if (loading) return;
    OptionDto warehouse = selectedOption(warehouseCombo);
    if (warehouse == null || warehouse.getId() == null) return;
    loading = true;
    Long maKho = warehouse.getId();
    String trangThai = selectedOptionCode(statusCombo);
    String fromDate;
    String toDate;
    try {
      fromDate = historyDateParam(false);
      toDate = historyDateParam(true);
    } catch (IllegalArgumentException ex) {
      loading = false;
      showWarning(ex.getMessage());
      return;
    }
    new SwingWorker<List<StocktakeDto>, Void>() {
      protected List<StocktakeDto> doInBackground() throws Exception {
        return apiClient.searchStocktakes(maKho, trangThai, fromDate, toDate, null);
      }

      protected void done() {
        try {
          List<StocktakeDto> rows = get();
          populateHistoryTable(rows);
          statusLabel.setText("Đã tải " + rows.size() + " phiếu kiểm kho.");
        } catch (Exception ex) {
          statusLabel.setText("Không tải được phiếu kiểm kho");
          JOptionPane.showMessageDialog(
              KiemKhoFrame.this, unwrapMessage(ex), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } finally {
          loading = false;
        }
      }
    }.execute();
  }

  private void populateStockTable(List<StocktakeSystemStockDto> rows) {
    stockModel.setRowCount(0);
    if (rows == null) return;
    for (StocktakeSystemStockDto row : rows) {
      stockModel.addRow(
          new Object[] {
            row.getMaLoHang(),
            valueOrDash(row.getTenNguyenLieu()),
            valueOrDash(row.getDonViTinh()),
            formatNumber(row.getSoLuongHeThong()),
            valueOrDash(row.getHanSuDung()),
            valueOrDash(row.getTrangThaiLo())
          });
    }
  }

  private void populateHistoryTable(List<StocktakeDto> rows) {
    historyModel.setRowCount(0);
    if (rows == null) return;
    for (StocktakeDto row : rows) {
      historyModel.addRow(
          new Object[] {
            row.getMaPhieuKiemKho(),
            formatDateTime(row.getNgayKiemKho()),
            valueOrDash(row.getTenKho()),
            statusLabelText(row.getTrangThai()),
            row.getSoDongChiTiet() == null ? 0 : row.getSoDongChiTiet(),
            valueOrDash(row.getTenNguoiKiem()),
            valueOrDash(row.getGhiChu())
          });
    }
  }

  private void populateItemTable() {
    itemModel.setRowCount(0);
    for (StocktakeLine line : draftLines) {
      itemModel.addRow(
          new Object[] {
            line.maLoHang(),
            valueOrDash(line.tenNguyenLieu()),
            valueOrDash(line.donViTinh()),
            formatNumber(line.soLuongHeThong()),
            formatNumber(line.soLuongThucTe()),
            formatNumber(line.soLuongThucTe().subtract(line.soLuongHeThong())),
            handlingLabel(line.huongXuLy()),
            valueOrDash(line.lyDoChenhLech())
          });
    }
  }

  private void fillSelectedStockToForm() {
    int row = stockTable.getSelectedRow();
    if (row < 0 || row >= stockTable.getRowCount()) return;
    Long lotId = Long.valueOf(stockTable.getValueAt(row, 0).toString());
    for (int i = 0; i < lotCombo.getItemCount(); i++) {
      StocktakeSystemStockDto stock = lotCombo.getItemAt(i);
      if (stock != null && lotId.equals(stock.getMaLoHang())) {
        lotCombo.setSelectedIndex(i);
        actualQtyField.requestFocusInWindow();
        return;
      }
    }
  }

  private void fillSelectedDraftLine() {
    int row = itemTable.getSelectedRow();
    if (row < 0 || row >= draftLines.size()) return;
    StocktakeLine line = draftLines.get(row);
    selectLot(line.maLoHang());
    actualQtyField.setText(inputNumber(line.soLuongThucTe()));
    reasonField.setText(line.lyDoChenhLech() == null ? "" : line.lyDoChenhLech());
    selectHandling(line.huongXuLy());
  }

  private void updateStockFieldsFromCombo() {
    StocktakeSystemStockDto stock = (StocktakeSystemStockDto) lotCombo.getSelectedItem();
    if (stock == null) {
      systemQtyField.setText("");
      diffQtyField.setText("");
      expiryField.setText("");
      return;
    }
    systemQtyField.setText(
        formatNumber(stock.getSoLuongHeThong()) + " " + valueOrDash(stock.getDonViTinh()));
    expiryField.setText(valueOrDash(stock.getHanSuDung()));
    updateDiffField();
  }

  private void updateDiffField() {
    StocktakeSystemStockDto stock = (StocktakeSystemStockDto) lotCombo.getSelectedItem();
    if (stock == null || actualQtyField.getText().isBlank()) {
      diffQtyField.setText("");
      return;
    }
    try {
      BigDecimal actual = parseDecimal(actualQtyField.getText());
      BigDecimal system =
          stock.getSoLuongHeThong() == null ? BigDecimal.ZERO : stock.getSoLuongHeThong();
      BigDecimal diff = actual.subtract(system);
      diffQtyField.setText(formatNumber(diff) + " " + valueOrDash(stock.getDonViTinh()));
      suggestHandlingForDiff(diff);
    } catch (NumberFormatException ignored) {
      diffQtyField.setText("");
    }
  }

  private void suggestHandlingForDiff(BigDecimal diff) {
    if (diff == null) {
      return;
    }
    if (diff.compareTo(BigDecimal.ZERO) == 0) {
      selectHandling("NO_ACTION");
    } else if (diff.compareTo(BigDecimal.ZERO) < 0) {
      selectHandling("CREATE_WASTAGE");
    } else {
      selectHandling("ADJUST_STOCK");
    }
  }

  private void addOrUpdateLine() {
    StocktakeSystemStockDto stock = (StocktakeSystemStockDto) lotCombo.getSelectedItem();
    OptionDto handling = selectedOption(handlingCombo);
    if (stock == null) {
      showWarning("Vui lòng chọn lô cần kiểm kho");
      return;
    }
    if (handling == null || handling.getCode() == null) {
      showWarning("Vui lòng chọn hướng xử lý");
      return;
    }
    BigDecimal actual =
        parseNonNegativeDecimal(
            actualQtyField.getText(), "Số lượng thực tế phải lớn hơn hoặc bằng 0");
    if (actual == null) return;
    BigDecimal system =
        stock.getSoLuongHeThong() == null ? BigDecimal.ZERO : stock.getSoLuongHeThong();
    if ("CREATE_WASTAGE".equals(handling.getCode()) && actual.compareTo(system) >= 0) {
      showWarning("Tạo hao hụt chỉ áp dụng khi số lượng thực tế nhỏ hơn hệ thống");
      return;
    }
    StocktakeLine line =
        new StocktakeLine(
            stock.getMaNguyenLieu(),
            stock.getTenNguyenLieu(),
            stock.getDonViTinh(),
            stock.getMaLoHang(),
            system,
            actual,
            reasonField.getText().trim(),
            handling.getCode());
    int existing = findDraftLineIndex(stock.getMaLoHang());
    if (existing >= 0) draftLines.set(existing, line);
    else draftLines.add(line);
    populateItemTable();
    actualQtyField.setText("");
    reasonField.setText("");
    updateStockFieldsFromCombo();
    statusLabel.setText("Đã thêm " + draftLines.size() + " dòng kiểm kho vào phiếu.");
  }

  private void removeSelectedLine() {
    int row = itemTable.getSelectedRow();
    if (row < 0 || row >= draftLines.size()) {
      showWarning("Vui lòng chọn dòng cần xóa");
      return;
    }
    draftLines.remove(row);
    populateItemTable();
  }

  private void saveStocktake(boolean completeAfterCreate) {
    OptionDto warehouse = selectedOption(warehouseCombo);
    if (warehouse == null || warehouse.getId() == null) {
      showWarning("Vui lòng chọn kho kiểm");
      return;
    }
    if (draftLines.isEmpty()) {
      showWarning("Phiếu kiểm kho cần ít nhất một dòng chi tiết");
      return;
    }
    StocktakeRequest request = new StocktakeRequest();
    request.setMaKho(warehouse.getId());
    request.setGhiChu(noteArea.getText().trim());
    request.setItems(buildRequestItems());
    setActionButtonsEnabled(false);
    statusLabel.setText(
        completeAfterCreate
            ? "Đang lưu và hoàn tất phiếu kiểm kho..."
            : "Đang lưu phiếu kiểm kho...");
    new SwingWorker<StocktakeDto, Void>() {
      protected StocktakeDto doInBackground() throws Exception {
        StocktakeDto saved =
            editingStocktakeId == null
                ? apiClient.createStocktake(request)
                : apiClient.updateStocktake(editingStocktakeId, request);
        return completeAfterCreate ? apiClient.completeStocktake(saved.getMaPhieuKiemKho()) : saved;
      }

      protected void done() {
        setActionButtonsEnabled(true);
        try {
          StocktakeDto response = get();
          JOptionPane.showMessageDialog(
              KiemKhoFrame.this,
              (completeAfterCreate ? "Đã hoàn tất phiếu kiểm kho #" : "Đã lưu phiếu kiểm kho #")
                  + response.getMaPhieuKiemKho()
                  + "\nKho: "
                  + response.getTenKho()
                  + "\nTrạng thái: "
                  + statusLabelText(response.getTrangThai()),
              "Kiểm kho",
              JOptionPane.INFORMATION_MESSAGE);
          resetDraft();
          loadSystemStockAndHistory();
        } catch (Exception ex) {
          statusLabel.setText("Lưu phiếu kiểm kho thất bại");
          JOptionPane.showMessageDialog(
              KiemKhoFrame.this, unwrapMessage(ex), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
      }
    }.execute();
  }

  private void openSelectedStocktake() {
    Long id = selectedHistoryId();
    if (id == null) {
      showWarning("Vui lòng chọn phiếu kiểm kho cần mở");
      return;
    }
    setActionButtonsEnabled(false);
    statusLabel.setText("Đang mở phiếu kiểm kho #" + id + "...");
    new SwingWorker<StocktakeDto, Void>() {
      protected StocktakeDto doInBackground() throws Exception {
        return apiClient.getById(id);
      }

      protected void done() {
        setActionButtonsEnabled(true);
        try {
          loadStocktakeIntoDraft(get());
        } catch (Exception ex) {
          JOptionPane.showMessageDialog(
              KiemKhoFrame.this, unwrapMessage(ex), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
      }
    }.execute();
  }

  private void cancelSelectedStocktake() {
    Long id = editingStocktakeId != null ? editingStocktakeId : selectedHistoryId();
    if (id == null) {
      showWarning("Vui lòng chọn phiếu nháp cần hủy");
      return;
    }
    int confirm =
        JOptionPane.showConfirmDialog(
            this, "Hủy phiếu kiểm kho #" + id + "?", "Kiểm kho", JOptionPane.YES_NO_OPTION);
    if (confirm != JOptionPane.YES_OPTION) {
      return;
    }
    setActionButtonsEnabled(false);
    new SwingWorker<StocktakeDto, Void>() {
      protected StocktakeDto doInBackground() throws Exception {
        return apiClient.cancelStocktake(id);
      }

      protected void done() {
        setActionButtonsEnabled(true);
        try {
          StocktakeDto response = get();
          JOptionPane.showMessageDialog(
              KiemKhoFrame.this,
              "Đã hủy phiếu kiểm kho #" + response.getMaPhieuKiemKho(),
              "Kiểm kho",
              JOptionPane.INFORMATION_MESSAGE);
          resetDraft();
          loadSystemStockAndHistory();
        } catch (Exception ex) {
          JOptionPane.showMessageDialog(
              KiemKhoFrame.this, unwrapMessage(ex), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
      }
    }.execute();
  }

  private Long selectedHistoryId() {
    int row = historyTable.getSelectedRow();
    if (row < 0 || row >= historyTable.getRowCount()) {
      return null;
    }
    Object value = historyTable.getValueAt(row, 0);
    return value == null ? null : Long.valueOf(value.toString());
  }

  private void loadStocktakeIntoDraft(StocktakeDto stocktake) {
    if (stocktake == null) {
      return;
    }
    if (!"DRAFT".equalsIgnoreCase(stocktake.getTrangThai())) {
      showWarning(
          "Chỉ mở lại được phiếu nháp. Phiếu này đang ở trạng thái "
              + statusLabelText(stocktake.getTrangThai()));
      return;
    }
    editingStocktakeId = stocktake.getMaPhieuKiemKho();
    noteArea.setText(stocktake.getGhiChu() == null ? "" : stocktake.getGhiChu());
    draftLines.clear();
    if (stocktake.getItems() != null) {
      for (StocktakeApiClient.StocktakeItemDto item : stocktake.getItems()) {
        draftLines.add(
            new StocktakeLine(
                item.getMaNguyenLieu(),
                item.getTenNguyenLieu(),
                item.getDonViTinh(),
                item.getMaLoHang(),
                item.getSoLuongHeThong() == null ? BigDecimal.ZERO : item.getSoLuongHeThong(),
                item.getSoLuongThucTe() == null ? BigDecimal.ZERO : item.getSoLuongThucTe(),
                item.getLyDoChenhLech(),
                item.getHuongXuLy()));
      }
    }
    populateItemTable();
    statusLabel.setText(
        "Đang chỉnh phiếu nháp #"
            + editingStocktakeId
            + ". Bấm Lưu nháp để cập nhật hoặc Hoàn tất để chốt phiếu.");
    updateActionButtons();
  }

  private List<StocktakeItemRequest> buildRequestItems() {
    List<StocktakeItemRequest> items = new ArrayList<>();
    for (StocktakeLine line : draftLines) {
      StocktakeItemRequest item = new StocktakeItemRequest();
      item.setMaNguyenLieu(line.maNguyenLieu());
      item.setMaLoHang(line.maLoHang());
      item.setSoLuongHeThong(line.soLuongHeThong());
      item.setSoLuongThucTe(line.soLuongThucTe());
      item.setLyDoChenhLech(line.lyDoChenhLech());
      item.setHuongXuLy(line.huongXuLy());
      items.add(item);
    }
    return items;
  }

  private void resetDraft() {
    editingStocktakeId = null;

    draftLines.clear();
    populateItemTable();

    noteArea.setText("");
    actualQtyField.setText("");
    reasonField.setText("");

    updateStockFieldsFromCombo();
    updateActionButtons();
  }

  private void setFormEnabled(boolean enabled) {
    warehouseCombo.setEnabled(enabled);
    ingredientCombo.setEnabled(enabled);
    statusCombo.setEnabled(enabled);
    lotCombo.setEnabled(enabled);
    handlingCombo.setEnabled(enabled);
    actualQtyField.setEnabled(enabled);
    reasonField.setEnabled(enabled);
    noteArea.setEnabled(enabled);
    setActionButtonsEnabled(enabled);
  }

  private void setActionButtonsEnabled(boolean enabled) {
    formActionsEnabled = enabled;
    updateActionButtons();
  }

  private void updateActionButtons() {
    if (!formActionsEnabled) {
      saveDraftButton.setEnabled(false);
      completeButton.setEnabled(false);
      openDraftButton.setEnabled(false);
      cancelDraftButton.setEnabled(false);
      return;
    }

    boolean canManage = PermissionUtil.hasAny("STOCKTAKE:MANAGE");

    boolean selectedDraft = selectedHistoryIsDraft();
    boolean editingDraft = editingStocktakeId != null;

    saveDraftButton.setEnabled(canManage);
    completeButton.setEnabled(canManage);
    openDraftButton.setEnabled(canManage && selectedDraft);
    cancelDraftButton.setEnabled(canManage && (editingDraft || selectedDraft));
  }

  private boolean selectedHistoryIsDraft() {
    int row = historyTable.getSelectedRow();
    if (row < 0 || row >= historyTable.getRowCount()) {
      return false;
    }
    Object value = historyTable.getValueAt(row, 3);
    return isDraftStatusText(value == null ? null : value.toString());
  }

  private boolean isDraftStatusText(String value) {
    if (value == null) {
      return false;
    }
    String text = value.trim();
    return "DRAFT".equalsIgnoreCase(text) || text.equalsIgnoreCase(statusLabelText("DRAFT"));
  }

  private String historyDateParam(boolean endOfDay) {
    String raw = historyDateField.getText();
    if (raw == null || raw.trim().isEmpty()) {
      return null;
    }

    try {
      LocalDate date = LocalDate.parse(raw.trim());
      return date + (endOfDay ? "T23:59:59" : "T00:00:00");
    } catch (Exception ex) {
      throw new IllegalArgumentException(
          "Ng\u00E0y phi\u1EBFu ph\u1EA3i c\u00F3 d\u1EA1ng yyyy-MM-dd, v\u00ED d\u1EE5 2026-05-23");
    }
  }

  private OptionDto[] withAllOption(String label, List<OptionDto> rows) {
    List<OptionDto> values = new ArrayList<>();
    OptionDto all = new OptionDto();
    all.setName(label);
    values.add(all);
    if (rows != null) values.addAll(rows);
    return values.toArray(new OptionDto[0]);
  }

  private OptionDto selectedOption(JComboBox<OptionDto> combo) {
    return (OptionDto) combo.getSelectedItem();
  }

  private Long selectedOptionId(JComboBox<OptionDto> combo) {
    OptionDto option = selectedOption(combo);
    return option == null ? null : option.getId();
  }

  private String selectedOptionCode(JComboBox<OptionDto> combo) {
    OptionDto option = selectedOption(combo);
    return option == null ? null : option.getCode();
  }

  private int findDraftLineIndex(Long maLoHang) {
    for (int i = 0; i < draftLines.size(); i++)
      if (maLoHang != null && maLoHang.equals(draftLines.get(i).maLoHang())) return i;
    return -1;
  }

  private void selectLot(Long maLoHang) {
    for (int i = 0; i < lotCombo.getItemCount(); i++) {
      StocktakeSystemStockDto item = lotCombo.getItemAt(i);
      if (item != null && maLoHang != null && maLoHang.equals(item.getMaLoHang())) {
        lotCombo.setSelectedIndex(i);
        return;
      }
    }
  }

  private void selectHandling(String code) {
    for (int i = 0; i < handlingCombo.getItemCount(); i++) {
      OptionDto item = handlingCombo.getItemAt(i);
      if (item != null && code != null && code.equals(item.getCode())) {
        handlingCombo.setSelectedIndex(i);
        return;
      }
    }
  }

  private BigDecimal parseNonNegativeDecimal(String raw, String message) {
    if (raw == null || raw.isBlank()) {
      showWarning(message);
      return null;
    }
    try {
      BigDecimal value = parseDecimal(raw);
      if (value.compareTo(BigDecimal.ZERO) < 0) {
        showWarning(message);
        return null;
      }
      return value;
    } catch (NumberFormatException ex) {
      showWarning(message);
      return null;
    }
  }

  private BigDecimal parseDecimal(String raw) {
    return new BigDecimal(raw.trim().replace(",", ""));
  }

  private JLabel sectionTitle(String text, int x, int y) {
    JLabel label = new JLabel(text);
    label.setBounds(x, y, 360, 24);
    label.setForeground(TEXT);
    label.setFont(UiTheme.bold(14));
    return label;
  }

  private RoundedPanel card(int x, int y, int w, int h) {
    RoundedPanel panel = new RoundedPanel(12, WHITE, SOFT_BORDER);
    panel.setLayout(null);
    panel.setBounds(x, y, w, h);
    return panel;
  }

  private void addLabel(JPanel parent, String text, int x, int y, int w, int h) {
    JLabel label = new JLabel(text);
    label.setBounds(x, y, w, h);
    label.setForeground(MUTED);
    label.setFont(UiTheme.regular(13));
    parent.add(label);
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
      JPanel parent, JTextField field, int x, int y, int w, int h, boolean editable) {
    styleField(field);
    field.setEditable(editable);
    field.setFocusable(editable);
    OutlinedInputPanel panel = new OutlinedInputPanel();
    panel.setLayout(new BorderLayout());
    panel.setBounds(x, y, w, h);
    panel.add(field, BorderLayout.CENTER);
    parent.add(panel);
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

  private void configureTable(JTable table, int statusColumn) {
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
    table.setDefaultRenderer(
        Object.class,
        new DefaultTableCellRenderer() {
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
              if (statusColumn >= 0 && column == statusColumn) {
                String text = String.valueOf(value);
                component.setForeground(
                    text.contains("Hoàn") ? SUCCESS : text.contains("Hủy") ? DANGER : TEXT);
              } else if (table == itemTable && column == 5) {
                component.setForeground(String.valueOf(value).startsWith("-") ? DANGER : SUCCESS);
              } else component.setForeground(TEXT);
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
  }

  private static RoundedButton primaryButton(String text) {
    return new RoundedButton(text).background(PRIMARY).hover(UiTheme.PRIMARY_DARK).radius(10);
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

  private String handlingLabel(String code) {
    if ("NO_ACTION".equalsIgnoreCase(code)) return "Không xử lý";
    if ("ADJUST_STOCK".equalsIgnoreCase(code)) return "Điều chỉnh tồn";
    if ("CREATE_WASTAGE".equalsIgnoreCase(code)) return "Tạo hao hụt";
    return valueOrDash(code);
  }

  private String statusLabelText(String code) {
    if ("DRAFT".equalsIgnoreCase(code)) return "Phiếu nháp";
    if ("COMPLETED".equalsIgnoreCase(code)) return "Hoàn tất";
    if ("CANCELLED".equalsIgnoreCase(code)) return "Đã hủy";
    return valueOrDash(code);
  }

  private String formatNumber(BigDecimal value) {
    return value == null ? "0" : numberFormat.format(value);
  }

  private String inputNumber(BigDecimal value) {
    return value == null ? "" : value.stripTrailingZeros().toPlainString();
  }

  private String formatDateTime(String value) {
    if (value == null || value.isBlank()) return "-";
    return value.length() > 19 ? value.substring(0, 19).replace('T', ' ') : value.replace('T', ' ');
  }

  private String valueOrDash(String value) {
    return value == null || value.isBlank() ? "-" : value;
  }

  private void showWarning(String message) {
    JOptionPane.showMessageDialog(this, message, "Kiểm kho", JOptionPane.WARNING_MESSAGE);
  }

  private String unwrapMessage(Exception ex) {
    Throwable current = ex;
    while (current.getCause() != null) current = current.getCause();
    return current.getMessage() == null ? "Không xử lý được yêu cầu" : current.getMessage();
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
    public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {}
  }

  private static class NameOnlyComboRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(
        JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      JLabel label =
          (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
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

  private static class DesignComboBoxRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(
        JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      JLabel label =
          (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      if (value instanceof OptionDto option) {
        String name = option.getName() == null ? "" : option.getName();
        String desc = option.getDescription();
        label.setText(desc == null || desc.isBlank() ? name : name + " - " + desc);
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

  private record StocktakeScreenData(
      List<StocktakeSystemStockDto> stocks, List<StocktakeDto> history) {}

  private record StocktakeLine(
      Long maNguyenLieu,
      String tenNguyenLieu,
      String donViTinh,
      Long maLoHang,
      BigDecimal soLuongHeThong,
      BigDecimal soLuongThucTe,
      String lyDoChenhLech,
      String huongXuLy) {}

  private class LotComboRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(
        JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      JLabel label =
          (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      if (value instanceof StocktakeSystemStockDto stock)
        label.setText(
            "Lô #"
                + stock.getMaLoHang()
                + " - "
                + formatNumber(stock.getSoLuongHeThong())
                + " "
                + valueOrDash(stock.getDonViTinh()));
      label.setFont(UiTheme.regular(14));
      label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 28));
      label.setForeground(TEXT);
      label.setBackground(isSelected && index >= 0 ? Color.decode("#F8DCC6") : WHITE);
      return label;
    }
  }
}
