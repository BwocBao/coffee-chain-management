package com.coffeechain.ui;

import com.coffeechain.service.ImageUploadApiClient;
import com.coffeechain.service.ImageUploadApiClient.UploadImageResponse;
import com.coffeechain.service.RecipeApiClient;
import com.coffeechain.service.RecipeApiClient.IngredientOption;
import com.coffeechain.service.RecipeApiClient.RecipeDetailDto;
import com.coffeechain.service.RecipeApiClient.RecipeIngredientLineDto;
import com.coffeechain.service.RecipeApiClient.RecipeIngredientRequest;
import com.coffeechain.service.RecipeApiClient.RecipeLookupDto;
import com.coffeechain.service.RecipeApiClient.RecipeRequest;
import com.coffeechain.service.RecipeApiClient.RecipeSummaryDto;
import com.coffeechain.service.RecipeApiClient.StatusOption;
import com.coffeechain.ui.common.IconLoader;
import com.coffeechain.ui.common.PermissionUtil;
import com.coffeechain.ui.common.RoundedButton;
import com.coffeechain.ui.common.UiTheme;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class QuanLyCongThucFrame extends JFrame {
  private static final int ROOT_W = 1440;
  private static final int ROOT_H = 820;
  private static final Color WHITE = Color.WHITE;
  private static final Color PRIMARY = UiTheme.PRIMARY;
  private static final Color PRIMARY_DARK = UiTheme.PRIMARY_DARK;
  private static final Color TEXT = UiTheme.TEXT_DARK;
  private static final Color MUTED = UiTheme.TEXT_MUTED;
  private static final Color BORDER = Color.decode("#D9C9BA");
  private static final Color SOFT_BORDER = Color.decode("#E6E6E6");
  private static final Color TABLE_HEAD = Color.decode("#F4E8DA");
  private static final Color SUCCESS = Color.decode("#248A52");
  private static final Color WARNING = Color.decode("#B98212");
  private static final Color DANGER = Color.decode("#BE3C2D");

  private final JPanel root = new JPanel(null);
  private final RecipeApiClient apiClient = new RecipeApiClient();
  private final ImageUploadApiClient imageUploadClient = new ImageUploadApiClient();
  private final DecimalFormat moneyFormat = new DecimalFormat("#,##0");
  private final DecimalFormat qtyFormat = new DecimalFormat("#,##0.###");
  private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  private final JTextField searchField =
      new IconPlaceholderTextField("Tìm công thức", new FlatSVGIcon("icons/tim.svg", 18, 18));
  private final JComboBox<StatusOption> filterStatusCombo = new JComboBox<>();
  private final JTextField productNameField = new JTextField();
  private final ImagePreviewPanel imagePreview = new ImagePreviewPanel();
  private final JTextField priceField = new JTextField();
  private final JComboBox<StatusOption> statusCombo = new JComboBox<>();
  private final JComboBox<IngredientOption> ingredientCombo = new JComboBox<>();
  private final JTextField qtyField = new JTextField();
  private final JLabel statusLabel = new JLabel(" ");
  private final JLabel selectedTitle = new JLabel("Chưa chọn công thức");
  private final JLabel selectedMeta =
      new JLabel("Chọn một dòng bên trái hoặc bấm Thêm mới công thức");
  private final JLabel totalCostLabel = new JLabel("0 VND");
  private final JLabel salePriceLabel = new JLabel("0 VND");
  private final JLabel grossMarginLabel = new JLabel("0%");

  private final RoundedButton saveButton = primaryButton("Lưu");
  private final RoundedButton newButton = primaryButton("+  Thêm mới công thức");
  private final RoundedButton deleteFormulaButton = dangerButton("Xóa CT");
  private final RoundedButton stopSellingButton = secondaryButton("Ngừng bán");
  private final RoundedButton clearButton = secondaryButton("Hủy");
  private final RoundedButton addIngredientButton = outlineButton("+  Thêm nguyên liệu");
  private final RoundedButton removeLineButton = dangerButton("Xóa dòng");

  private final DefaultTableModel summaryModel =
      new DefaultTableModel(
          new Object[] {"Mã CT", "Sản phẩm", "Trạng thái", "Ngày tạo", "SL NL"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return false;
        }
      };
  private final JTable summaryTable = new JTable(summaryModel);
  private final DefaultTableModel ingredientModel =
      new DefaultTableModel(
          new Object[] {"STT", "Nguyên liệu", "ĐVT", "Định mức", "Giá vốn", "Thành tiền"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return false;
        }
      };
  private final JTable ingredientTable = new JTable(ingredientModel);

  private final List<RecipeSummaryDto> recipes = new ArrayList<>();
  private final List<IngredientOption> ingredients = new ArrayList<>();
  private final List<StatusOption> statuses = new ArrayList<>();
  private final List<RecipeIngredientLineDto> currentLines = new ArrayList<>();
  private Long selectedProductId;
  private String imageUrl;
  private boolean loading;

  public QuanLyCongThucFrame() {
    setTitle("Phụng Lộc - Quản lý công thức");
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setResizable(false);
    root.setPreferredSize(new Dimension(ROOT_W, ROOT_H));
    root.setBackground(WHITE);
    setContentPane(root);
    buildHeader();
    buildToolbar();
    buildList();
    buildDetail();
    bindEvents();
    pack();
    setLocationRelativeTo(null);
    setupColumnWidths();
    loadLookupsAndData();
  }

  private void buildHeader() {
    JLabel breadcrumb = new JLabel("HOME  ›  Quản lý POS  ›  Quản lý công thức");
    breadcrumb.setBounds(44, 18, 420, 24);
    breadcrumb.setForeground(MUTED);
    breadcrumb.setFont(UiTheme.regular(13));
    root.add(breadcrumb);
    JLabel title = new JLabel("QUẢN LÝ CÔNG THỨC");
    title.setBounds(44, 58, 520, 44);
    title.setForeground(PRIMARY_DARK);
    title.setFont(UiTheme.bold(32));
    root.add(title);
    RoundedButton backButton = primaryButton("Quay lại");
    backButton.setIcon(IconLoader.svg("icons/nhap-kho/left.svg", 16, 18));
    backButton.setIconTextGap(8);
    backButton.setHorizontalAlignment(SwingConstants.CENTER);
    backButton.setBounds(1250, 58, 110, 34);
    backButton.addActionListener(
        e -> {
          new QuanLyPOSFrame().setVisible(true);
          dispose();
        });
    root.add(backButton);
  }

  private void buildToolbar() {
    addFieldPanel(root, searchField, 44, 126, 300, 42);
    addCombo(root, filterStatusCombo, 362, 126, 230, 42);
    newButton.setBounds(612, 126, 240, 42);
    root.add(newButton);
  }

  private void buildList() {
    RoundedCard card = new RoundedCard(16, WHITE, SOFT_BORDER);
    card.setLayout(null);
    card.setBounds(44, 190, 590, 570);
    root.add(card);
    JLabel title = sectionTitle("Danh sách công thức");
    title.setBounds(20, 18, 250, 26);
    card.add(title);
    statusLabel.setBounds(20, 44, 420, 22);
    statusLabel.setForeground(MUTED);
    statusLabel.setFont(UiTheme.regular(13));
    card.add(statusLabel);
    configureTable(summaryTable);
    summaryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane scrollPane = hiddenScrollPane(summaryTable);
    scrollPane.setBounds(0, 82, 590, 430);
    card.add(scrollPane);
    RoundedButton refreshButton = secondaryButton("Tải lại");
    refreshButton.setBounds(458, 526, 92, 32);
    refreshButton.addActionListener(e -> loadRecipes());
    card.add(refreshButton);
  }

  private void buildDetail() {
    RoundedCard card = new RoundedCard(16, WHITE, SOFT_BORDER);
    card.setLayout(null);
    card.setBounds(650, 190, 746, 570);
    root.add(card);
    JLabel title = sectionTitle("Chi tiết công thức & định giá");
    title.setBounds(20, 18, 320, 26);
    card.add(title);
    selectedTitle.setBounds(20, 50, 330, 26);
    selectedTitle.setFont(UiTheme.bold(18));
    selectedTitle.setForeground(PRIMARY_DARK);
    card.add(selectedTitle);
    selectedMeta.setBounds(20, 76, 430, 22);
    selectedMeta.setForeground(MUTED);
    selectedMeta.setFont(UiTheme.regular(13));
    card.add(selectedMeta);

    addLabel(card, "Tên sản phẩm", 20, 114, 100, 20);
    addFieldPanel(card, productNameField, 20, 138, 180, 34);
    addLabel(card, "Giá bán", 220, 114, 90, 20);
    addFieldPanel(card, priceField, 220, 138, 110, 34);
    addLabel(card, "Trạng thái", 350, 114, 100, 20);
    addCombo(card, statusCombo, 350, 138, 180, 34);
    addLabel(card, "Tên sản phẩm", 20, 114, 120, 20);
    addLabel(card, "Hình ảnh", 590, 50, 120, 20);
    imagePreview.setBounds(590, 74, 122, 128);
    imagePreview.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            chooseAndUploadImage();
          }
        });
    card.add(imagePreview);

    JLabel ingTitle = sectionTitle("Danh sách nguyên liệu");
    ingTitle.setBounds(20, 194, 260, 24);
    card.add(ingTitle);
    configureTable(ingredientTable);
    JScrollPane ingredientScroll = hiddenScrollPane(ingredientTable);
    ingredientScroll.setBounds(20, 224, 540, 210);
    card.add(ingredientScroll);

    RoundedCard priceCard = new RoundedCard(12, Color.decode("#FFF8F0"), Color.decode("#EAD7C4"));
    priceCard.setLayout(null);
    priceCard.setBounds(576, 224, 150, 210);
    card.add(priceCard);
    addLabel(priceCard, "Tổng giá vốn", 14, 16, 120, 20);
    totalCostLabel.setBounds(14, 40, 124, 28);
    totalCostLabel.setForeground(PRIMARY_DARK);
    totalCostLabel.setFont(UiTheme.bold(16));
    priceCard.add(totalCostLabel);
    addLabel(priceCard, "Giá bán", 14, 82, 120, 20);
    salePriceLabel.setBounds(14, 106, 124, 28);
    salePriceLabel.setForeground(PRIMARY_DARK);
    salePriceLabel.setFont(UiTheme.bold(16));
    priceCard.add(salePriceLabel);
    addLabel(priceCard, "Lợi nhuận gộp", 14, 148, 120, 20);
    grossMarginLabel.setBounds(14, 172, 124, 28);
    grossMarginLabel.setForeground(SUCCESS);
    grossMarginLabel.setFont(UiTheme.bold(16));
    priceCard.add(grossMarginLabel);

    addLabel(card, "Nguyên liệu", 20, 450, 100, 18);
    addCombo(card, ingredientCombo, 20, 470, 250, 34);
    addLabel(card, "Định mức", 286, 450, 90, 18);
    addFieldPanel(card, qtyField, 286, 470, 110, 34);
    addIngredientButton.setBounds(412, 470, 150, 34);
    card.add(addIngredientButton);
    removeLineButton.setBounds(576, 470, 110, 34);
    card.add(removeLineButton);

    saveButton.setBounds(300, 522, 100, 36);
    clearButton.setBounds(416, 522, 100, 36);
    stopSellingButton.setBounds(532, 522, 100, 36);
    deleteFormulaButton.setBounds(648, 522, 78, 36);
    card.add(saveButton);
    card.add(clearButton);
    card.add(stopSellingButton);
    card.add(deleteFormulaButton);
  }

  private void bindEvents() {
    searchField.addActionListener(e -> loadRecipes());
    newButton.addActionListener(e -> clearForm());
    saveButton.addActionListener(e -> saveRecipe());
    clearButton.addActionListener(e -> clearForm());
    addIngredientButton.addActionListener(e -> addIngredientLine());
    removeLineButton.addActionListener(e -> removeIngredientLine());
    deleteFormulaButton.addActionListener(e -> deleteFormula());
    stopSellingButton.addActionListener(e -> stopSelling());
    filterStatusCombo.addActionListener(
        e -> {
          if (!loading) loadRecipes();
        });
    summaryTable
        .getSelectionModel()
        .addListSelectionListener(
            e -> {
              if (!e.getValueIsAdjusting()) loadSelectedDetail();
            });
  }

  private void loadLookupsAndData() {
    loading = true;
    setButtonsEnabled(false);
    showStatus("Đang tải dữ liệu công thức...", false);
    new SwingWorker<RecipeLookupDto, Void>() {
      @Override
      protected RecipeLookupDto doInBackground() throws Exception {
        return apiClient.getLookups();
      }

      @Override
      protected void done() {
        try {
          RecipeLookupDto lookup = get();
          ingredients.clear();
          statuses.clear();
          if (lookup != null && lookup.getIngredients() != null)
            ingredients.addAll(lookup.getIngredients());
          if (lookup != null && lookup.getStatuses() != null) statuses.addAll(lookup.getStatuses());
          populateCombos();
          loadRecipes();
        } catch (Exception ex) {
          showError("Không tải được lookup", ex);
        } finally {
          loading = false;
          applyPermissions();
        }
      }
    }.execute();
  }

  private void populateCombos() {
    filterStatusCombo.removeAllItems();
    filterStatusCombo.addItem(new StatusOption(null, "Tất cả trạng thái"));
    statusCombo.removeAllItems();
    for (StatusOption status : statuses) {
      filterStatusCombo.addItem(status);
      statusCombo.addItem(status);
    }
    if (statusCombo.getItemCount() == 0) {
      statusCombo.addItem(new StatusOption("AVAILABLE", "Có sẵn"));
      statusCombo.addItem(new StatusOption("STOP_SELLING", "Ngừng bán"));
    }
    ingredientCombo.removeAllItems();
    for (IngredientOption ingredient : ingredients) ingredientCombo.addItem(ingredient);
  }

  private void loadRecipes() {
    loading = true;
    setButtonsEnabled(false);
    new SwingWorker<List<RecipeSummaryDto>, Void>() {
      @Override
      protected List<RecipeSummaryDto> doInBackground() throws Exception {
        return apiClient.searchRecipes(searchText(), selectedFilterStatus());
      }

      @Override
      protected void done() {
        try {
          recipes.clear();
          recipes.addAll(get());
          populateSummaryTable();
          showStatus("Tổng số: " + recipes.size() + " công thức", false);
        } catch (Exception ex) {
          showError("Tra cứu công thức thất bại", ex);
        } finally {
          loading = false;
          applyPermissions();
        }
      }
    }.execute();
  }

  private void populateSummaryTable() {
    summaryModel.setRowCount(0);
    for (RecipeSummaryDto r : recipes)
      summaryModel.addRow(
          new Object[] {
            valueOrDash(r.getMaCongThucHienThi()),
            valueOrDash(r.getTenSanPham()),
            statusText(r.getTrangThai()),
            formatDate(r.getNgayTao()),
            r.getSoNguyenLieu() == null ? 0 : r.getSoNguyenLieu()
          });
  }

  private void loadSelectedDetail() {
    int row = summaryTable.getSelectedRow();
    if (row < 0 || row >= recipes.size()) return;
    RecipeSummaryDto summary = recipes.get(summaryTable.convertRowIndexToModel(row));
    new SwingWorker<RecipeDetailDto, Void>() {
      @Override
      protected RecipeDetailDto doInBackground() throws Exception {
        return apiClient.getRecipeDetail(summary.getMaSanPham());
      }

      @Override
      protected void done() {
        try {
          fillDetail(get());
        } catch (Exception ex) {
          showError("Không tải được chi tiết công thức", ex);
        }
      }
    }.execute();
  }

  private void fillDetail(RecipeDetailDto detail) {
    selectedProductId = detail.getMaSanPham();
    selectedTitle.setText(valueOrDash(detail.getTenSanPham()));
    selectedMeta.setText(
        valueOrDash(detail.getMaCongThucHienThi())
            + "  •  "
            + statusText(detail.getTrangThai())
            + "  •  "
            + formatDate(detail.getNgayTao()));
    productNameField.setText(valueOrEmpty(detail.getTenSanPham()));
    imageUrl = valueOrEmpty(detail.getHinhAnh());
    imagePreview.setImageUrl(imageUrl);
    priceField.setText(inputNumber(detail.getGiaBanHienTai()));
    selectStatus(detail.getTrangThai());
    currentLines.clear();
    if (detail.getItems() != null) currentLines.addAll(detail.getItems());
    refreshIngredientTable();
    updatePricing();
    applyPermissions();
  }

  private void clearForm() {
    selectedProductId = null;
    summaryTable.clearSelection();
    selectedTitle.setText("Công thức mới");
    selectedMeta.setText("Nhập sản phẩm và thêm nguyên liệu bên dưới");
    productNameField.setText("");
    imageUrl = null;
    imagePreview.clear();
    priceField.setText("");
    qtyField.setText("");
    if (statusCombo.getItemCount() > 0) statusCombo.setSelectedIndex(0);
    currentLines.clear();
    refreshIngredientTable();
    updatePricing();
    applyPermissions();
  }

  private void addIngredientLine() {
    IngredientOption ingredient = (IngredientOption) ingredientCombo.getSelectedItem();
    if (ingredient == null || ingredient.getId() == null) {
      showWarning("Vui lòng chọn nguyên liệu");
      return;
    }
    BigDecimal qty = parsePositive(qtyField.getText(), "Định mức");
    if (qty == null) return;
    for (RecipeIngredientLineDto line : currentLines)
      if (ingredient.getId().equals(line.getMaNguyenLieu())) {
        showWarning("Nguyên liệu đã có trong công thức");
        return;
      }
    RecipeIngredientLineDto line = new RecipeIngredientLineDto();
    line.setMaNguyenLieu(ingredient.getId());
    line.setTenNguyenLieu(ingredient.getName());
    line.setDonViTinh(ingredient.getUnit());
    line.setSoLuongCan(qty);
    line.setGiaVonDvt(ingredient.getGiaVonDvt());
    line.setThanhTien(qty.multiply(nullToZero(ingredient.getGiaVonDvt())));
    currentLines.add(line);
    qtyField.setText("");
    refreshIngredientTable();
    updatePricing();
  }

  private void removeIngredientLine() {
    int row = ingredientTable.getSelectedRow();
    if (row < 0) {
      showWarning("Vui lòng chọn dòng nguyên liệu cần xóa");
      return;
    }
    currentLines.remove(ingredientTable.convertRowIndexToModel(row));
    refreshIngredientTable();
    updatePricing();
  }

  private void refreshIngredientTable() {
    ingredientModel.setRowCount(0);
    int i = 1;
    for (RecipeIngredientLineDto line : currentLines) {
      BigDecimal thanhTien =
          nullToZero(line.getSoLuongCan()).multiply(nullToZero(line.getGiaVonDvt()));
      line.setThanhTien(thanhTien);
      ingredientModel.addRow(
          new Object[] {
            i++,
            valueOrDash(line.getTenNguyenLieu()),
            valueOrDash(line.getDonViTinh()),
            formatQty(line.getSoLuongCan()),
            formatMoney(line.getGiaVonDvt()),
            formatMoney(thanhTien)
          });
    }
  }

  private void updatePricing() {
    BigDecimal total = BigDecimal.ZERO;
    for (RecipeIngredientLineDto line : currentLines)
      total = total.add(nullToZero(line.getThanhTien()));
    BigDecimal sale = parseNumberOrZero(priceField.getText());
    totalCostLabel.setText(formatMoney(total) + " VND");
    salePriceLabel.setText(formatMoney(sale) + " VND");
    if (sale.compareTo(BigDecimal.ZERO) > 0)
      grossMarginLabel.setText(
          sale.subtract(total)
                  .multiply(BigDecimal.valueOf(100))
                  .divide(sale, 1, RoundingMode.HALF_UP)
              + "%");
    else grossMarginLabel.setText("0%");
  }

  private void saveRecipe() {
    RecipeRequest req = buildRequest();
    if (req == null) return;
    boolean creating = selectedProductId == null;
    Long id = selectedProductId;
    setButtonsEnabled(false);
    new SwingWorker<RecipeDetailDto, Void>() {
      @Override
      protected RecipeDetailDto doInBackground() throws Exception {
        return creating ? apiClient.createRecipe(req) : apiClient.updateRecipe(id, req);
      }

      @Override
      protected void done() {
        try {
          RecipeDetailDto d = get();
          fillDetail(d);
          loadRecipes();
          showStatus("Đã lưu công thức", false);
        } catch (Exception ex) {
          showError("Lưu công thức thất bại", ex);
        } finally {
          applyPermissions();
        }
      }
    }.execute();
  }

  private RecipeRequest buildRequest() {
    if (productNameField.getText().trim().isEmpty()) {
      showWarning("Vui lòng nhập tên sản phẩm");
      return null;
    }
    BigDecimal price = parsePositive(priceField.getText(), "Giá bán");
    if (price == null) return null;
    if (currentLines.isEmpty()) {
      showWarning("Công thức phải có ít nhất một nguyên liệu");
      return null;
    }
    RecipeRequest req = new RecipeRequest();
    req.setTenSanPham(productNameField.getText().trim());
    req.setHinhAnh(imageUrl == null ? null : imageUrl.trim());
    req.setGiaBanHienTai(price);
    req.setTrangThai(selectedStatus());
    List<RecipeIngredientRequest> items = new ArrayList<>();
    for (RecipeIngredientLineDto line : currentLines)
      items.add(new RecipeIngredientRequest(line.getMaNguyenLieu(), line.getSoLuongCan()));
    req.setItems(items);
    return req;
  }

  private void deleteFormula() {
    if (selectedProductId == null) {
      showWarning("Vui lòng chọn công thức");
      return;
    }
    if (JOptionPane.showConfirmDialog(
            this,
            "Xóa công thức hiện hành của sản phẩm này?",
            "Xóa công thức",
            JOptionPane.YES_NO_OPTION)
        != JOptionPane.YES_OPTION) return;
    Long id = selectedProductId;
    new SwingWorker<RecipeDetailDto, Void>() {
      @Override
      protected RecipeDetailDto doInBackground() throws Exception {
        return apiClient.deleteFormula(id);
      }

      @Override
      protected void done() {
        try {
          fillDetail(get());
          loadRecipes();
        } catch (Exception ex) {
          showError("Xóa công thức thất bại", ex);
        }
      }
    }.execute();
  }

  private void stopSelling() {
    if (selectedProductId == null) {
      showWarning("Vui lòng chọn sản phẩm");
      return;
    }
    Long id = selectedProductId;
    new SwingWorker<RecipeDetailDto, Void>() {
      @Override
      protected RecipeDetailDto doInBackground() throws Exception {
        return apiClient.stopSelling(id);
      }

      @Override
      protected void done() {
        try {
          fillDetail(get());
          loadRecipes();
        } catch (Exception ex) {
          showError("Ngừng bán thất bại", ex);
        }
      }
    }.execute();
  }

  private void chooseAndUploadImage() {
    if (!PermissionUtil.hasAny("RECIPE:MANAGE")) {
      showWarning("Bạn không có quyền upload hình ảnh");
      return;
    }

    FileDialog dialog = new FileDialog(this, "Ch?n h?nh ?nh s?n ph?m", FileDialog.LOAD);
    dialog.setFilenameFilter(
        (dir, name) -> {
          String lower = name == null ? "" : name.toLowerCase();
          return lower.endsWith(".jpg")
              || lower.endsWith(".jpeg")
              || lower.endsWith(".png")
              || lower.endsWith(".webp")
              || lower.endsWith(".gif");
        });
    dialog.setVisible(true);

    java.io.File[] files = dialog.getFiles();
    if (files == null || files.length == 0) {
      return;
    }

    Path imagePath = files[0].toPath();
    imagePreview.setLoading(true);
    setButtonsEnabled(false);
    showStatus("Đang upload hình ảnh...", false);

    new SwingWorker<UploadImageResponse, Void>() {
      @Override
      protected UploadImageResponse doInBackground() throws Exception {
        return imageUploadClient.uploadImage(imagePath);
      }

      @Override
      protected void done() {
        try {
          UploadImageResponse response = get();
          imageUrl = firstNonBlank(response.getSecureUrl(), response.getUrl());
          imagePreview.setImageUrl(imageUrl);
          showStatus("Đã upload hình ảnh", false);
        } catch (Exception ex) {
          imagePreview.setLoading(false);
          showError("Upload hình ảnh thất bại", ex);
        } finally {
          applyPermissions();
        }
      }
    }.execute();
  }

  private String searchText() {
    return searchField.getText().trim();
  }

  private String selectedFilterStatus() {
    StatusOption s = (StatusOption) filterStatusCombo.getSelectedItem();
    return s == null ? null : s.getCode();
  }

  private String selectedStatus() {
    StatusOption s = (StatusOption) statusCombo.getSelectedItem();
    return s == null ? "AVAILABLE" : s.getCode();
  }

  private void selectStatus(String code) {
    for (int i = 0; i < statusCombo.getItemCount(); i++)
      if (code != null && code.equals(statusCombo.getItemAt(i).getCode())) {
        statusCombo.setSelectedIndex(i);
        return;
      }
  }

  private void applyPermissions() {
    boolean canManage = PermissionUtil.hasAny("RECIPE:MANAGE");
    setButtonsEnabled(canManage && !loading);
  }

  private void setButtonsEnabled(boolean enabled) {
    saveButton.setEnabled(enabled);
    deleteFormulaButton.setEnabled(enabled && selectedProductId != null);
    stopSellingButton.setEnabled(enabled && selectedProductId != null);
    clearButton.setEnabled(enabled);
    addIngredientButton.setEnabled(enabled);
    removeLineButton.setEnabled(enabled);
    newButton.setEnabled(!loading && PermissionUtil.hasAny("RECIPE:MANAGE"));
  }

  private BigDecimal parsePositive(String raw, String label) {
    try {
      BigDecimal v = new BigDecimal(raw.trim().replace(",", ""));
      if (v.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException();
      return v;
    } catch (Exception e) {
      showWarning(label + " phải là số lớn hơn 0");
      return null;
    }
  }

  private BigDecimal parseNumberOrZero(String raw) {
    try {
      return new BigDecimal(raw.trim().replace(",", ""));
    } catch (Exception e) {
      return BigDecimal.ZERO;
    }
  }

  private BigDecimal nullToZero(BigDecimal v) {
    return v == null ? BigDecimal.ZERO : v;
  }

  private String inputNumber(BigDecimal v) {
    return v == null ? "" : v.stripTrailingZeros().toPlainString();
  }

  private String formatMoney(BigDecimal v) {
    return moneyFormat.format(nullToZero(v));
  }

  private String formatQty(BigDecimal v) {
    return qtyFormat.format(nullToZero(v));
  }

  private String formatDate(java.time.LocalDateTime dt) {
    return dt == null ? "-" : dateFormat.format(dt);
  }

  private String statusText(String code) {
    if ("AVAILABLE".equals(code)) return "Có sẵn";
    if ("OUT_OF_STOCK".equals(code)) return "Chưa có công thức";
    if ("STOP_SELLING".equals(code)) return "Ngừng bán";
    return valueOrDash(code);
  }

  private String valueOrDash(String v) {
    return v == null || v.isBlank() ? "-" : v;
  }

  private String valueOrEmpty(String v) {
    return v == null ? "" : v;
  }

  private String firstNonBlank(String first, String second) {
    return first != null && !first.isBlank() ? first : second;
  }

  private void showStatus(String msg, boolean error) {
    statusLabel.setForeground(error ? DANGER : MUTED);
    statusLabel.setText(msg);
  }

  private void showWarning(String msg) {
    JOptionPane.showMessageDialog(this, msg, "Quản lý công thức", JOptionPane.WARNING_MESSAGE);
  }

  private void showError(String title, Exception ex) {
    String msg = unwrapMessage(ex);
    showStatus(title + ": " + msg, true);
    JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
  }

  private String unwrapMessage(Exception ex) {
    Throwable c = ex;
    while (c.getCause() != null) c = c.getCause();
    return c.getMessage() == null ? "Không xử lý được yêu cầu" : c.getMessage();
  }

  private void addFieldPanel(JPanel parent, JTextField field, int x, int y, int w, int h) {
    styleField(field);
    RoundedInputPanel p = new RoundedInputPanel();
    p.setLayout(new BorderLayout());
    p.setBounds(x, y, w, h);
    p.add(field, BorderLayout.CENTER);
    parent.add(p);
  }

  private void addCombo(JPanel parent, JComboBox<?> combo, int x, int y, int w, int h) {
    styleCombo(combo);
    OutlinedInputPanel p = new OutlinedInputPanel();
    p.setLayout(new BorderLayout());
    p.setBounds(x, y, w, h);
    p.add(combo, BorderLayout.CENTER);
    parent.add(p);
  }

  private void addLabel(JPanel p, String text, int x, int y, int w, int h) {
    JLabel l = new JLabel(text);
    l.setBounds(x, y, w, h);
    l.setForeground(MUTED);
    l.setFont(UiTheme.regular(13));
    p.add(l);
  }

  private JLabel sectionTitle(String text) {
    JLabel l = new JLabel(text);
    l.setForeground(TEXT);
    l.setFont(UiTheme.bold(16));
    return l;
  }

  private void styleField(JTextField f) {
    f.setFont(UiTheme.regular(14));
    f.setForeground(TEXT);
    f.setBorder(
        BorderFactory.createEmptyBorder(0, f instanceof IconPlaceholderTextField ? 48 : 12, 0, 12));
    f.setOpaque(false);
  }

  private void styleCombo(JComboBox<?> c) {
    c.setUI(new DesignComboBoxUI());
    c.setRenderer(new DesignComboBoxRenderer());
    c.setFont(UiTheme.regular(14));
    c.setBackground(WHITE);
    c.setForeground(TEXT);
    c.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 8));
    c.setOpaque(false);
    c.setFocusable(false);
    c.setMaximumRowCount(8);
  }

  private void configureTable(JTable t) {
    t.setRowHeight(40);
    t.setFont(UiTheme.regular(13));
    t.getTableHeader().setFont(UiTheme.bold(13));
    t.getTableHeader().setBackground(TABLE_HEAD);
    t.getTableHeader().setPreferredSize(new Dimension(0, 38));
    t.setSelectionBackground(Color.decode("#F8DCC6"));
    t.setGridColor(SOFT_BORDER);
    t.setShowGrid(true);
    t.setDefaultRenderer(
        Object.class,
        new DefaultTableCellRenderer() {
          @Override
          public Component getTableCellRendererComponent(
              JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            Component c =
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            if (!isSelected) {
              c.setForeground(TEXT);
              if (col == 2 && table == summaryTable) {
                String v = String.valueOf(value);
                c.setForeground(
                    v.contains("Có")
                        ? SUCCESS
                        : v.contains("Chưa") ? WARNING : v.contains("Ngừng") ? DANGER : TEXT);
              }
            }
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

  private static RoundedButton primaryButton(String text) {
    return new RoundedButton(text).background(PRIMARY).hover(PRIMARY_DARK).radius(10);
  }

  private static RoundedButton secondaryButton(String text) {
    RoundedButton b =
        new RoundedButton(text)
            .background(Color.decode("#D8D8D8"))
            .hover(Color.decode("#C5C5C5"))
            .radius(10);
    b.setForeground(TEXT);
    return b;
  }

  private static RoundedButton dangerButton(String text) {
    return new RoundedButton(text).background(DANGER).hover(Color.decode("#A83427")).radius(10);
  }

  private static RoundedButton outlineButton(String text) {
    RoundedButton b =
        new RoundedButton(text).background(WHITE).hover(Color.decode("#FFF4EA")).radius(10);
    b.setForeground(PRIMARY_DARK);
    b.setBorder(BorderFactory.createLineBorder(PRIMARY));
    return b;
  }

  private static class ImagePreviewPanel extends JPanel {
    private String imageUrl;
    private BufferedImage image;
    private boolean loading;

    ImagePreviewPanel() {
      setOpaque(false);
      setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      setToolTipText("Click để upload hinh ảnh sản phẩm");
    }

    void clear() {
      imageUrl = null;
      image = null;
      loading = false;
      repaint();
    }

    void setLoading(boolean loading) {
      this.loading = loading;
      repaint();
    }

    void setImageUrl(String imageUrl) {
      this.imageUrl = imageUrl;
      this.image = null;
      this.loading = imageUrl != null && !imageUrl.isBlank();
      repaint();
      if (this.loading) {
        loadImageAsync(imageUrl);
      }
    }

    private void loadImageAsync(String url) {
      new SwingWorker<BufferedImage, Void>() {
        @Override
        protected BufferedImage doInBackground() throws Exception {
          return ImageIO.read(new URL(url));
        }

        @Override
        protected void done() {
          try {
            image = get();
          } catch (Exception ignored) {
            image = null;
          } finally {
            loading = false;
            repaint();
          }
        }
      }.execute();
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);

      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

      int arc = 10;
      g2.setColor(WHITE);
      g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);

      if (image != null) {
        int padding = 8;
        int targetX = padding;
        int targetY = padding;
        int targetW = Math.max(1, getWidth() - padding * 2);
        int targetH = Math.max(1, getHeight() - padding * 2);

        double scale = Math.max(targetW / (double) image.getWidth(), targetH / (double) image.getHeight());
        int drawW = Math.max(1, (int) Math.round(image.getWidth() * scale));
        int drawH = Math.max(1, (int) Math.round(image.getHeight() * scale));
        int x = targetX + (targetW - drawW) / 2;
        int y = targetY + (targetH - drawH) / 2;

        Shape oldClip = g2.getClip();
        g2.setClip(
            new java.awt.geom.RoundRectangle2D.Float(targetX, targetY, targetW, targetH, 8, 8));
        g2.drawImage(image, x, y, drawW, drawH, null);
        g2.setClip(oldClip);
      } else {
        String text = loading ? "Dang tai anh..." : "Click de tai anh";
        g2.setColor(MUTED);
        g2.setFont(UiTheme.regular(12));
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        int y = getHeight() / 2 + fm.getAscent() / 2 - 2;
        g2.drawString(text, Math.max(8, x), y);
      }

      g2.setClip(null);
      g2.setStroke(new BasicStroke(1.3f));
      g2.setColor(BORDER);
      g2.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, arc, arc);
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
      // OutlinedInputPanel da ve nen va vien.
    }
  }

  private static class DesignComboBoxRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(
        JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      JLabel label =
          (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      if (value instanceof StatusOption status)
        label.setText(status.getName() == null ? status.getCode() : status.getName());
      if (value instanceof IngredientOption ingredient) label.setText(ingredient.getName());
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

  private static class RoundedCard extends JPanel {
    private final int r;
    private final Color fill;
    private final Color border;

    RoundedCard(int r, Color fill, Color border) {
      this.r = r;
      this.fill = fill;
      this.border = border;
      setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(fill);
      g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, r, r);
      g2.setColor(border);
      g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, r, r);
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

  private void setupColumnWidths() {
    // Bảng summaryTable
    summaryTable.getColumnModel().getColumn(0).setPreferredWidth(70); // Mã CT
    summaryTable.getColumnModel().getColumn(1).setPreferredWidth(180); // Sản phẩm
    summaryTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Trạng thái
    summaryTable.getColumnModel().getColumn(3).setPreferredWidth(130); // Ngày tạo
    summaryTable.getColumnModel().getColumn(4).setPreferredWidth(70); // SL NL

    // Bảng ingredientTable
    ingredientTable.getColumnModel().getColumn(0).setPreferredWidth(50); // STT
    ingredientTable.getColumnModel().getColumn(1).setPreferredWidth(180); // Nguyên liệu
    ingredientTable.getColumnModel().getColumn(2).setPreferredWidth(70); // ĐVT
    ingredientTable.getColumnModel().getColumn(3).setPreferredWidth(90); // Định mức
    ingredientTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Giá vốn
    ingredientTable.getColumnModel().getColumn(5).setPreferredWidth(110); // Thành tiền
  }
}
