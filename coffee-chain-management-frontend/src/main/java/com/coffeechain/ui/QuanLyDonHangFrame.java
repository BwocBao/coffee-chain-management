package com.coffeechain.ui;

import com.coffeechain.service.PosApiClient;
import com.coffeechain.service.PosApiClient.BankQrDto;
import com.coffeechain.service.PosApiClient.CreatePosOrderRequest;
import com.coffeechain.service.PosApiClient.OptionDto;
import com.coffeechain.service.PosApiClient.PosOrderDto;
import com.coffeechain.service.PosApiClient.PosOrderItemDto;
import com.coffeechain.service.PosApiClient.PosOrderItemRequest;
import com.coffeechain.service.PosApiClient.PosOrderSummaryDto;
import com.coffeechain.service.PosApiClient.PosProductDto;
import com.coffeechain.ui.common.IconLoader;
import com.coffeechain.ui.common.RoundedButton;
import com.coffeechain.ui.common.UiTheme;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.Component;
import java.math.BigDecimal;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.DefaultListCellRenderer;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class QuanLyDonHangFrame extends JFrame {
  private static final Color BG = Color.decode("#FAF7F4");
  private static final Color WHITE = Color.WHITE;
  private static final Color ACCENT = UiTheme.PRIMARY;
  private static final Color ACCENT_DARK = UiTheme.PRIMARY_DARK;
  private static final Color TEXT = UiTheme.TEXT_DARK;
  private static final Color MUTED = UiTheme.TEXT_MUTED;
  private static final Color BORDER = Color.decode("#E5D8CC");
  private static final Color ROW_SELECT = Color.decode("#FDE1CC");
  private static final Color DANGER = Color.decode("#BE3C2D");
  private static final Color SUCCESS = Color.decode("#248A52");

  private final PosApiClient apiClient = new PosApiClient();
  private final DecimalFormat moneyFormat = new DecimalFormat("#,##0");
  private final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
  private final JPanel root = new JPanel(new BorderLayout());
  private final CardLayout cardLayout = new CardLayout();
  private final JPanel mainContent = new JPanel(cardLayout);
  private final JButton[] tabButtons = new JButton[2];
  private final String[] tabNames = {"T\u1ea1o \u0111\u01a1n h\u00e0ng", "Thanh to\u00e1n"};
  private final JTextField productSearchField = createSearchField("Tìm sản phẩm");
  private final JPanel productGrid = new JPanel(new GridLayout(0, 4, 12, 12));
  private final JPanel cartList = new JPanel();
  private final JLabel cartTotalLabel = new JLabel("0 VND");
  private final JComboBox<OptionDto> branchCombo = new JComboBox<>();
  private final JComboBox<OptionDto> posCombo = new JComboBox<>();
  private final JTextField orderSearchField = createSearchField("Tìm mã đơn");
  private final JComboBox<StatusOption> orderStatusCombo = new JComboBox<>();
  private final DefaultTableModel orderModel = new DefaultTableModel(new Object[] {"Mã đơn", "Ngày", "Chi nhánh", "Trạng thái", "Thanh toán", "Tổng tiền"}, 0) {
    @Override public boolean isCellEditable(int row, int column) { return false; }
  };
  private final JTable orderTable = new JTable(orderModel);
  private final JPanel orderDetailPanel = new JPanel(new BorderLayout());
  private final List<PosProductDto> products = new ArrayList<>();
  private final List<OptionDto> branches = new ArrayList<>();
  private final List<OptionDto> posDevices = new ArrayList<>();
  private final Map<Long, CartLine> cart = new LinkedHashMap<>();
  private int activeTab;
  private Long pendingOpenOrderId;

  public QuanLyDonHangFrame() {
    setTitle("Phụng Lộc - Quản lý đơn hàng POS");
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setSize(1240, 760);
    setMinimumSize(new Dimension(1120, 680));
    setLocationRelativeTo(null);
    root.setBackground(BG);
    setContentPane(root);
    buildTopBar();
    buildContent();
    loadInitialData();
  }
  private void buildTopBar() {
    JPanel top = new JPanel(new BorderLayout());
    top.setOpaque(false);
    top.setBorder(new EmptyBorder(16, 24, 0, 24));
    JPanel titles = new JPanel(new BorderLayout(0, 4));
    titles.setOpaque(false);
    JLabel title = new JLabel("QUẢN LÝ ĐƠN HÀNG");
    title.setForeground(ACCENT_DARK);
    title.setFont(UiTheme.bold(30));
    JLabel subtitle = new JLabel("T\u1ea1o h\u00f3a \u0111\u01a1n POS v\u00e0 x\u00e1c nh\u1eadn thanh to\u00e1n");
    subtitle.setForeground(MUTED);
    subtitle.setFont(UiTheme.regular(14));
    titles.add(title, BorderLayout.NORTH);
    titles.add(subtitle, BorderLayout.SOUTH);
    top.add(titles, BorderLayout.WEST);
    RoundedButton back = primaryButton("Quay lại");
    back.setIcon(IconLoader.svg("icons/nhap-kho/left.svg", 16, 18));
    back.setIconTextGap(8);
    back.setHorizontalAlignment(SwingConstants.CENTER);
    back.setPreferredSize(new Dimension(110, 36));
    back.addActionListener(e -> { new QuanLyPOSFrame().setVisible(true); dispose(); });
    top.add(back, BorderLayout.EAST);
    JPanel tabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    tabs.setOpaque(false);
    tabs.setBorder(new EmptyBorder(14, 0, 0, 0));
    for (int i = 0; i < tabNames.length; i++) {
      int idx = i;
      tabButtons[i] = createTabButton(tabNames[i]);
      tabButtons[i].addActionListener(e -> switchTab(idx));
      tabs.add(tabButtons[i]);
    }
    top.add(tabs, BorderLayout.SOUTH);
    root.add(top, BorderLayout.NORTH);
  }

  private JButton createTabButton(String text) {
    JButton button = new JButton(text) {
      @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        boolean selected = getText().equals(tabNames[activeTab]);
        if (selected) {
          g2.setColor(Color.decode("#FFF1E7"));
          g2.fillRoundRect(8, 5, getWidth() - 16, getHeight() - 10, 10, 10);
          g2.setColor(ACCENT);
          g2.fillRoundRect(20, getHeight() - 5, getWidth() - 40, 3, 3, 3);
        }
        g2.setColor(selected ? ACCENT_DARK : MUTED);
        g2.setFont(selected ? UiTheme.bold(14) : UiTheme.regular(14));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, 29);
        g2.dispose();
      }
    };
    button.setPreferredSize(new Dimension(160, 44));
    button.setOpaque(false);
    button.setContentAreaFilled(false);
    button.setBorderPainted(false);
    button.setFocusPainted(false);
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    return button;
  }

  private void buildContent() {
    mainContent.setOpaque(false);
    mainContent.add(buildCreateOrderPanel(), "0");
    root.add(mainContent, BorderLayout.CENTER);
  }

  private JPanel buildCreateOrderPanel() {
    JPanel panel = new JPanel(new BorderLayout(18, 0));
    panel.setOpaque(false);
    panel.setBorder(new EmptyBorder(18, 24, 20, 24));
    JPanel left = new JPanel(new BorderLayout(0, 12));
    left.setOpaque(false);
    JPanel filter = new JPanel(new GridLayout(1, 3, 12, 0));
    filter.setOpaque(false);
    filter.add(wrapField("Tìm sản phẩm", productSearchField));
    filter.add(wrapCombo("Chi nhánh", branchCombo));
    filter.add(wrapCombo("Máy POS", posCombo));
    left.add(filter, BorderLayout.NORTH);
    productSearchField.getDocument().addDocumentListener(doc(this::renderProducts));
    branchCombo.addActionListener(e -> refreshPosCombo());
    productGrid.setOpaque(false);
    left.add(hiddenScrollPane(productGrid), BorderLayout.CENTER);
    JPanel right = buildCartPanel();
    right.setPreferredSize(new Dimension(350, 0));
    panel.add(left, BorderLayout.CENTER);
    panel.add(right, BorderLayout.EAST);
    return panel;
  }

  private JPanel buildCartPanel() {
    RoundedPanel panel = new RoundedPanel();
    panel.setLayout(new BorderLayout(0, 10));
    panel.setBorder(new EmptyBorder(16, 16, 16, 16));
    JLabel title = new JLabel("Giỏ hàng hiện tại");
    title.setFont(UiTheme.bold(18));
    panel.add(title, BorderLayout.NORTH);
      cartList.setOpaque(false);
      cartList.setLayout(new BoxLayout(cartList, BoxLayout.Y_AXIS));
    panel.add(hiddenScrollPane(cartList), BorderLayout.CENTER);
    JPanel footer = new JPanel(new BorderLayout(0, 10));
    footer.setOpaque(false);
    JPanel totalRow = new JPanel(new BorderLayout());
    totalRow.setOpaque(false);
    totalRow.add(new JLabel("Tổng tiền"), BorderLayout.WEST);
    cartTotalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    cartTotalLabel.setFont(UiTheme.bold(22));
    totalRow.add(cartTotalLabel, BorderLayout.EAST);
    footer.add(totalRow, BorderLayout.NORTH);
    RoundedButton create = new RoundedButton("Tạo đơn hàng");
    create.setPreferredSize(new Dimension(0, 40));
    create.addActionListener(e -> createOrder());
    footer.add(create, BorderLayout.CENTER);
    panel.add(footer, BorderLayout.SOUTH);
    return panel;
  }
  private JPanel buildOrderActionPanel(boolean cancelMode) {
    JPanel panel = new JPanel(new BorderLayout(18, 0));
    panel.setOpaque(false);
    panel.setBorder(new EmptyBorder(18, 24, 20, 24));
    JPanel left = new RoundedPanel();
    left.setLayout(new BorderLayout(0, 12));
    left.setBorder(new EmptyBorder(14, 14, 14, 14));
    JPanel topFilters = new JPanel(new BorderLayout(0, 6));
    topFilters.setOpaque(false);
    JPanel filters = new JPanel(new GridLayout(1, 2, 10, 0));
    filters.setOpaque(false);
    filters.add(wrapField("Tìm mã đơn", orderSearchField));
    filters.add(wrapCombo("Trạng thái", orderStatusCombo));
    JLabel hint = new JLabel("Ch\u1ecdn \u0111\u01a1n ch\u1edd thanh to\u00e1n \u0111\u1ec3 h\u1ee7y ho\u1eb7c x\u00e1c nh\u1eadn thanh to\u00e1n");
    hint.setForeground(MUTED);
    hint.setFont(UiTheme.regular(12));
    topFilters.add(filters, BorderLayout.CENTER);
    topFilters.add(hint, BorderLayout.SOUTH);
    left.add(topFilters, BorderLayout.NORTH);
    configureOrderTable();
    left.add(hiddenScrollPane(orderTable), BorderLayout.CENTER);
    panel.add(left, BorderLayout.CENTER);
    orderDetailPanel.setOpaque(false);
    orderDetailPanel.setPreferredSize(new Dimension(380, 0));
    panel.add(orderDetailPanel, BorderLayout.EAST);
    orderSearchField.getDocument().addDocumentListener(doc(this::loadOrders));
    orderStatusCombo.addActionListener(e -> loadOrders());
    orderTable.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) loadSelectedOrder(false); });
    return panel;
  }

  private void configureOrderTable() {
    orderTable.setRowHeight(38);
    orderTable.setFont(UiTheme.regular(13));
    orderTable.getTableHeader().setFont(UiTheme.bold(13));
    orderTable.getTableHeader().setBackground(Color.decode("#F4E8DA"));
    orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    orderTable.setSelectionBackground(ROW_SELECT);
    orderTable.setShowGrid(true);
    orderTable.setGridColor(BORDER);
    orderTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
      @Override public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        label.setBorder(new EmptyBorder(0, 10, 0, 10));
        String status = String.valueOf(table.getValueAt(row, 3));
        if (!isSelected) label.setBackground(WHITE);
        label.setForeground((column == 3 || column == 4) && status.contains("Hoàn tất") ? SUCCESS : TEXT);
        return label;
      }
    });
  }

  private void loadInitialData() {
    new SwingWorker<Void, Void>() {
      private Exception error;
      @Override protected Void doInBackground() {
        try {
          PosApiClient.PosLookupDto lookup = apiClient.getLookups();
          products.clear(); products.addAll(apiClient.getProducts());
          branches.clear(); branches.addAll(lookup.getBranches());
          posDevices.clear(); posDevices.addAll(lookup.getPosDevices());
        } catch (Exception ex) { error = ex; }
        return null;
      }
      @Override protected void done() {
        if (error != null) { showError("Không tải được dữ liệu POS: " + error.getMessage()); return; }
        branchCombo.setModel(new DefaultComboBoxModel<>(branches.toArray(new OptionDto[0])));
        styleCombo(branchCombo);
        styleCombo(posCombo);
        styleCombo(orderStatusCombo);
        refreshPosCombo(); initStatusCombo(); renderProducts(); renderCart(); loadOrders();
      }
    }.execute();
  }

  private void initStatusCombo() {
    orderStatusCombo.setModel(new DefaultComboBoxModel<>(new StatusOption[] {
      new StatusOption("", "Tất cả trạng thái"), new StatusOption("PENDING", "Chờ thanh toán"),
      new StatusOption("COMPLETED", "Hoàn tất"), new StatusOption("CANCELLED", "Đã hủy"), new StatusOption("PAID", "Đã thanh toán")
    }));
  }

  private void refreshPosCombo() {
    OptionDto branch = selectedBranch();
    DefaultComboBoxModel<OptionDto> model = new DefaultComboBoxModel<>();
    if (branch != null) for (OptionDto pos : posDevices) if (String.valueOf(branch.getId()).equals(pos.getCode())) model.addElement(pos);
    posCombo.setModel(model);
  }

  private void renderProducts() {
    productGrid.removeAll();
    String keyword = text(productSearchField).toLowerCase();
    for (PosProductDto product : products) {
      if (keyword.isEmpty() || safe(product.getTenSanPham()).toLowerCase().contains(keyword)) productGrid.add(productCard(product));
    }
    productGrid.revalidate(); productGrid.repaint();
  }

  private JPanel productCard(PosProductDto product) {
    RoundedPanel card = new RoundedPanel();
    card.setLayout(new BorderLayout(0, 8));
    card.setBorder(new EmptyBorder(10, 10, 10, 10));
    ProductImagePanel image = new ProductImagePanel(product.getHinhAnh(), safe(product.getTenSanPham()));
    image.setPreferredSize(new Dimension(0, 96));
    card.add(image, BorderLayout.NORTH);
    JLabel name = new JLabel(ellipsis(product.getTenSanPham(), 24));
    name.setFont(UiTheme.regular(13));
    card.add(name, BorderLayout.CENTER);
    JPanel bottom = new JPanel(new BorderLayout()); bottom.setOpaque(false);
    JLabel price = new JLabel(formatMoney(product.getGiaBanHienTai())); price.setFont(UiTheme.bold(13));
      RoundedButton add = new RoundedButton("+");
      add.setPreferredSize(new Dimension(38, 28));
      add.addActionListener(e -> showQtyDialog(product));
    bottom.add(price, BorderLayout.WEST); bottom.add(add, BorderLayout.EAST); card.add(bottom, BorderLayout.SOUTH);
    return card;
  }

    private void showQtyDialog(PosProductDto product) {
        CartLine existing = cart.get(product.getMaSanPham());
        int initQty = existing != null ? existing.quantity : 1;

        JDialog dialog = new JDialog(this, "Nhập số lượng", true);
        dialog.setSize(310, 235);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.setUndecorated(true);

        JPanel content =
                new JPanel(new BorderLayout(0, 12)) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        g2.setColor(WHITE);
                        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

                        g2.setColor(BORDER);
                        g2.setStroke(new BasicStroke(1.2f));
                        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 16, 16);

                        g2.dispose();
                        super.paintComponent(g);
                    }
                };

        content.setOpaque(false);
        content.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel title = new JLabel("Nhập số lượng");
        title.setFont(UiTheme.bold(15));
        title.setForeground(ACCENT_DARK);

        JLabel productName = new JLabel(safe(product.getTenSanPham()));
        productName.setFont(UiTheme.regular(13));
        productName.setForeground(MUTED);

        int[] qty = {initQty};

        JButton minus = circleButton("−");
        JButton plus = circleButton("+");

        JTextField qtyField = new JTextField(String.valueOf(qty[0]), 4);
        qtyField.setFont(UiTheme.bold(14));
        qtyField.setHorizontalAlignment(JTextField.CENTER);
        qtyField.setPreferredSize(new Dimension(64, 32));
        qtyField.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        qtyField.setOpaque(false);

        JPanel qtyRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        qtyRow.setOpaque(false);
        qtyRow.add(minus);
        qtyRow.add(qtyField);
        qtyRow.add(plus);

        JLabel unitPrice = new JLabel(formatMoney(product.getGiaBanHienTai()));
        unitPrice.setHorizontalAlignment(SwingConstants.RIGHT);
        unitPrice.setFont(UiTheme.regular(12));
        unitPrice.setForeground(TEXT);

        JLabel subtotal =
                new JLabel(formatMoney(nullToZero(product.getGiaBanHienTai()).multiply(BigDecimal.valueOf(qty[0]))));
        subtotal.setHorizontalAlignment(SwingConstants.RIGHT);
        subtotal.setFont(UiTheme.bold(12));
        subtotal.setForeground(TEXT);

        Runnable updateSubtotal =
                () -> {
                    try {
                        int value = Integer.parseInt(qtyField.getText().trim());
                        if (value < 1) value = 1;
                        qty[0] = value;
                        BigDecimal amount = nullToZero(product.getGiaBanHienTai()).multiply(BigDecimal.valueOf(qty[0]));
                        subtotal.setText(formatMoney(amount));
                    } catch (Exception ex) {
                        subtotal.setText(formatMoney(BigDecimal.ZERO));
                    }
                };

        minus.addActionListener(
                e -> {
                    if (qty[0] > 1) {
                        qty[0]--;
                        qtyField.setText(String.valueOf(qty[0]));
                        updateSubtotal.run();
                    }
                });

        plus.addActionListener(
                e -> {
                    qty[0]++;
                    qtyField.setText(String.valueOf(qty[0]));
                    updateSubtotal.run();
                });

        qtyField
                .getDocument()
                .addDocumentListener(
                        new DocumentListener() {
                            @Override
                            public void insertUpdate(DocumentEvent e) {
                                updateSubtotal.run();
                            }

                            @Override
                            public void removeUpdate(DocumentEvent e) {
                                updateSubtotal.run();
                            }

                            @Override
                            public void changedUpdate(DocumentEvent e) {
                                updateSubtotal.run();
                            }
                        });

        JPanel pricePanel = new JPanel(new GridLayout(2, 2, 4, 4));
        pricePanel.setOpaque(false);

        JLabel unitLabel = new JLabel("Đơn giá");
        unitLabel.setFont(UiTheme.regular(12));
        unitLabel.setForeground(MUTED);

        JLabel subtotalLabel = new JLabel("Thành tiền");
        subtotalLabel.setFont(UiTheme.regular(12));
        subtotalLabel.setForeground(MUTED);

        pricePanel.add(unitLabel);
        pricePanel.add(unitPrice);
        pricePanel.add(subtotalLabel);
        pricePanel.add(subtotal);

        JPanel middle = new JPanel(new BorderLayout(0, 10));
        middle.setOpaque(false);
        middle.add(productName, BorderLayout.NORTH);
        middle.add(qtyRow, BorderLayout.CENTER);
        middle.add(pricePanel, BorderLayout.SOUTH);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 8, 0));
        buttons.setOpaque(false);

        RoundedButton confirm = primaryButton("Xác nhận");
        RoundedButton cancel = secondaryButton("Hủy");

        confirm.addActionListener(
                e -> {
                    int finalQty;
                    try {
                        finalQty = Integer.parseInt(qtyField.getText().trim());
                        if (finalQty <= 0) finalQty = 1;
                    } catch (Exception ex) {
                        finalQty = 1;
                    }

                    CartLine line = cart.computeIfAbsent(product.getMaSanPham(), id -> new CartLine(product));
                    line.quantity = finalQty;

                    dialog.dispose();
                    renderCart();
                });

        cancel.addActionListener(e -> dialog.dispose());

        buttons.add(confirm);
        buttons.add(cancel);

        content.add(title, BorderLayout.NORTH);
        content.add(middle, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }

  private void addProduct(PosProductDto product) {
    CartLine line = cart.computeIfAbsent(product.getMaSanPham(), id -> new CartLine(product));
    line.quantity++;
    renderCart();
  }

    private void renderCart() {
        cartList.removeAll();

        int index = 0;
        int size = cart.size();

        for (CartLine line : cart.values()) {
            cartList.add(cartRow(line));

            if (index < size - 1) {
                JSeparator sep = new JSeparator();
                sep.setForeground(BORDER);
                sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

                JPanel sepWrap = new JPanel(new BorderLayout());
                sepWrap.setOpaque(false);
                sepWrap.setBorder(new EmptyBorder(4, 0, 4, 0));
                sepWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 9));
                sepWrap.add(sep, BorderLayout.CENTER);

                cartList.add(sepWrap);
            }

            index++;
        }

        cartTotalLabel.setText(formatMoney(cartTotal()));
        cartList.revalidate();
        cartList.repaint();
    }

    private JPanel cartRow(CartLine line) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(6, 0, 6, 0));
        row.setPreferredSize(new Dimension(0, 54));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));

        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel(safe(line.product.getTenSanPham()));
        nameLabel.setFont(UiTheme.bold(13));
        nameLabel.setForeground(TEXT);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel priceLabel = new JLabel(formatMoney(line.product.getGiaBanHienTai()));
        priceLabel.setFont(UiTheme.regular(12));
        priceLabel.setForeground(MUTED);
        priceLabel.setBorder(new EmptyBorder(2, 0, 0, 0));
        priceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(nameLabel);
        infoPanel.add(priceLabel);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);
        actions.setPreferredSize(new Dimension(150, 36));

        JButton minus = cartActionButton("minus", false);
        JButton plus = cartActionButton("plus", false);
        JButton del = cartActionButton("close", true);

        JLabel qtyLabel = new JLabel(String.valueOf(line.quantity), SwingConstants.CENTER);
        qtyLabel.setFont(UiTheme.bold(13));
        qtyLabel.setForeground(TEXT);
        qtyLabel.setPreferredSize(new Dimension(20, 30));

        minus.addActionListener(e -> {
            line.quantity--;
            if (line.quantity <= 0) {
                cart.remove(line.product.getMaSanPham());
            }
            renderCart();
        });

        plus.addActionListener(e -> {
            line.quantity++;
            renderCart();
        });

        del.addActionListener(e -> {
            cart.remove(line.product.getMaSanPham());
            renderCart();
        });

        actions.add(minus);
        actions.add(qtyLabel);
        actions.add(plus);
        actions.add(del);

        row.add(infoPanel, BorderLayout.CENTER);
        row.add(actions, BorderLayout.EAST);

        return row;
    }

    private JButton cartActionButton(String type, boolean danger) {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean pressed = getModel().isPressed();
                boolean hover = getModel().isRollover();

                Color bg;
                Color borderColor;
                Color iconColor;

                if (danger) {
                    bg = pressed ? Color.decode("#F7D6D1")
                            : hover ? Color.decode("#FBE5E1")
                            : Color.decode("#FFF3F1");
                    borderColor = Color.decode("#E7B8B1");
                    iconColor = DANGER;
                } else {
                    bg = pressed ? Color.decode("#E8D6C8")
                            : hover ? Color.decode("#F5E8DD")
                            : Color.decode("#FAF3ED");
                    borderColor = Color.decode("#D8C2AF");
                    iconColor = ACCENT_DARK;
                }

                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

                g2.setColor(iconColor);
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int cx = getWidth() / 2;
                int cy = getHeight() / 2;

                switch (type) {
                    case "minus" -> {
                        g2.drawLine(cx - 5, cy, cx + 5, cy);
                    }
                    case "plus" -> {
                        g2.drawLine(cx - 5, cy, cx + 5, cy);
                        g2.drawLine(cx, cy - 5, cx, cy + 5);
                    }
                    case "close" -> {
                        g2.drawLine(cx - 4, cy - 4, cx + 4, cy + 4);
                        g2.drawLine(cx + 4, cy - 4, cx - 4, cy + 4);
                    }
                }

                g2.dispose();
            }
        };

        button.setPreferredSize(new Dimension(30, 30));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return button;
    }

    private JButton circleButton(String text) {
        JButton button =
                new JButton(text) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        Color bg =
                                getModel().isPressed()
                                        ? Color.decode("#D8C8BA")
                                        : getModel().isRollover()
                                        ? Color.decode("#F7E4D5")
                                        : Color.decode("#EFE4DA");

                        g2.setColor(bg);
                        g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);

                        g2.setColor(TEXT);
                        g2.setFont(UiTheme.bold(15));

                        FontMetrics fm = g2.getFontMetrics();
                        int x = (getWidth() - fm.stringWidth(getText())) / 2;
                        int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;

                        g2.drawString(getText(), x, y);
                        g2.dispose();
                    }
                };

        button.setPreferredSize(new Dimension(32, 32));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return button;
    }


  private void createOrder() {
    OptionDto branch = selectedBranch(); OptionDto pos = selectedPos();
    if (branch == null || pos == null) { showWarning("Vui lòng chọn chi nhánh và máy POS"); return; }
    if (cart.isEmpty()) { showWarning("Vui lòng chọn ít nhất một sản phẩm"); return; }
    CreatePosOrderRequest request = new CreatePosOrderRequest();
    request.setMaChiNhanh(branch.getId()); request.setMaPos(pos.getId());
    List<PosOrderItemRequest> items = new ArrayList<>();
    for (CartLine line : cart.values()) items.add(new PosOrderItemRequest(line.product.getMaSanPham(), line.quantity));
    request.setItems(items);
    new SwingWorker<PosOrderDto, Void>() {
      private Exception error;
      @Override protected PosOrderDto doInBackground() { try { return apiClient.createOrder(request); } catch (Exception ex) { error = ex; return null; } }
      @Override protected void done() {
        if (error != null) { showError("Không tạo được đơn hàng: " + error.getMessage()); return; }
        try {
          PosOrderDto order = get();
          cart.clear();
          renderCart();
          pendingOpenOrderId = order == null ? null : order.getMaHoaDon();
          orderSearchField.setText("");
          if (orderStatusCombo.getItemCount() > 0) orderStatusCombo.setSelectedIndex(0);
          showInfo("Tạo đơn hàng thành công. Chuyển sang thanh toán đơn #" + pendingOpenOrderId);
          switchTab(1);
        } catch (Exception ex) {
          showError("Không mở được đơn vừa tạo: " + ex.getMessage());
        }
      }
    }.execute();
  }

  private void loadOrders() {
    Long branchId = selectedBranch() == null ? null : selectedBranch().getId();
    String keyword = text(orderSearchField); StatusOption status = (StatusOption) orderStatusCombo.getSelectedItem(); String statusCode = status == null ? null : status.code;
    new SwingWorker<List<PosOrderSummaryDto>, Void>() {
      private Exception error;
      @Override protected List<PosOrderSummaryDto> doInBackground() { try { return apiClient.searchOrders(branchId, keyword, statusCode); } catch (Exception ex) { error = ex; return List.of(); } }
      @Override protected void done() { if (error != null) { showError("Không tải được danh sách đơn hàng: " + error.getMessage()); return; } try { populateOrders(get()); } catch (Exception ignored) {} }
    }.execute();
  }

  private void populateOrders(List<PosOrderSummaryDto> orders) {
    orderModel.setRowCount(0);
    int rowToSelect = -1;
    for (PosOrderSummaryDto order : orders) {
      if (pendingOpenOrderId != null && pendingOpenOrderId.equals(order.getMaHoaDon())) {
        rowToSelect = orderModel.getRowCount();
      }
      orderModel.addRow(new Object[] {
        order.getMaHoaDon(), order.getThoiGianTaoHoaDon() == null ? "" : dateTimeFormat.format(order.getThoiGianTaoHoaDon()), order.getTenChiNhanh(),
        displayOrderStatus(order.getTrangThaiHoaDon()), displayPaymentStatus(order.getTrangThaiThanhToan(), order.getPhuongThucThanhToan()), formatMoney(order.getTongThanhToan())
      });
    }
    if (rowToSelect >= 0) {
      orderTable.getSelectionModel().setSelectionInterval(rowToSelect, rowToSelect);
      orderTable.scrollRectToVisible(orderTable.getCellRect(rowToSelect, 0, true));
      pendingOpenOrderId = null;
    }
  }

  private void loadSelectedOrder(boolean cancelMode) {
    int row = orderTable.getSelectedRow(); if (row < 0) return;
    Long id = ((Number) orderTable.getValueAt(row, 0)).longValue();
    new SwingWorker<PosOrderDto, Void>() {
      private Exception error;
      @Override protected PosOrderDto doInBackground() { try { return apiClient.getOrder(id); } catch (Exception ex) { error = ex; return null; } }
      @Override protected void done() { if (error != null) { showError("Không tải được chi tiết đơn: " + error.getMessage()); return; } try { renderOrderDetail(get(), cancelMode); } catch (Exception ignored) {} }
    }.execute();
  }

  private void renderOrderDetail(PosOrderDto order, boolean cancelMode) {
    orderDetailPanel.removeAll();
    RoundedPanel card = new RoundedPanel();
    card.setLayout(new BorderLayout(0, 10));
    card.setBorder(new EmptyBorder(16, 16, 16, 16));

    JPanel header = new JPanel(new BorderLayout(0, 4));
    header.setOpaque(false);
    JLabel title = new JLabel("Đơn #" + order.getMaHoaDon());
    title.setFont(UiTheme.bold(18));
    title.setForeground(ACCENT_DARK);
    JLabel status = new JLabel(paymentSummary(order));
    status.setFont(UiTheme.regular(13));
    status.setForeground(isPaid(order) ? SUCCESS : MUTED);
    header.add(title, BorderLayout.NORTH);
    header.add(status, BorderLayout.SOUTH);
    card.add(header, BorderLayout.NORTH);

    JPanel list = new JPanel();
    list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
    list.setOpaque(false);
    for (PosOrderItemDto item : order.getItems()) {
      JLabel line = new JLabel(safe(item.getTenSanPham()) + "  x" + item.getSoLuong() + "  -  " + formatMoney(item.getThanhTienDong()));
      line.setBorder(new EmptyBorder(6, 0, 6, 0));
      line.setFont(UiTheme.regular(13));
      list.add(line);
      list.add(new JSeparator());
    }
    card.add(hiddenScrollPane(list), BorderLayout.CENTER);

    JPanel footer = new JPanel(new GridLayout(0, 1, 0, 8));
    footer.setOpaque(false);
    JLabel total = new JLabel("Tổng: " + formatMoney(order.getTongThanhToan()));
    total.setFont(UiTheme.bold(20));
    footer.add(total);

    boolean pending = isPending(order);
    boolean paid = isPaid(order);
    if (paid) {
      JLabel done = new JLabel("Thanh toán hoàn tất");
      done.setForeground(SUCCESS);
      done.setFont(UiTheme.bold(15));
      footer.add(done);
      RoundedButton print = primaryButton("In hóa đơn");
      print.addActionListener(e -> printReceipt(order));
      footer.add(print);
    } else {
      RoundedButton cancel = dangerButton("H\u1ee7y \u0111\u01a1n");
      cancel.setEnabled(pending);
      cancel.addActionListener(e -> cancelOrder(order.getMaHoaDon()));
      RoundedButton cash = primaryButton("Thanh to\u00e1n ti\u1ec1n m\u1eb7t");
      cash.setEnabled(pending);
      cash.addActionListener(e -> payCash(order.getMaHoaDon()));
      RoundedButton bank = secondaryButton("Thanh to\u00e1n QR");
      bank.setEnabled(pending);
      bank.addActionListener(e -> createBankQr(order.getMaHoaDon()));
      footer.add(cancel);
      footer.add(cash);
      footer.add(bank);
      if (!pending) {
        JLabel note = new JLabel("Ch\u1ec9 h\u1ee7y ho\u1eb7c thanh to\u00e1n \u0111\u01b0\u1ee3c \u0111\u01a1n \u0111ang ch\u1edd thanh to\u00e1n.");
        note.setForeground(MUTED);
        note.setFont(UiTheme.regular(12));
        footer.add(note);
      }
    }
    card.add(footer, BorderLayout.SOUTH);
    orderDetailPanel.add(card, BorderLayout.CENTER);
    orderDetailPanel.revalidate();
    orderDetailPanel.repaint();
  }
  private void payCash(Long maHoaDon) { runOrderAction("Thanh toán tiền mặt", () -> apiClient.payCash(maHoaDon), false); }
  private void cancelOrder(Long maHoaDon) {
    int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn hủy đơn #" + maHoaDon + "?", "Hủy đơn", JOptionPane.YES_NO_OPTION);
    if (confirm == JOptionPane.YES_OPTION) runOrderAction("Hủy đơn hàng", () -> apiClient.cancelOrder(maHoaDon), true);
  }
  private void createBankQr(Long maHoaDon) {
    new SwingWorker<BankQrDto, Void>() {
      private Exception error;
      @Override protected BankQrDto doInBackground() { try { return apiClient.createBankQr(maHoaDon); } catch (Exception ex) { error = ex; return null; } }
      @Override protected void done() {
        if (error != null) { showError("Không tạo được QR: " + error.getMessage()); return; }
        try {
          BankQrDto qr = get();
          showQrDialog(qr);
        } catch (Exception ex) { showError("Không hiển thị được QR thanh toán: " + ex.getMessage()); }
      }
    }.execute();
  }
  private void runOrderAction(String actionName, OrderAction action, boolean clearDetail) {
    new SwingWorker<PosOrderDto, Void>() {
      private Exception error;
      @Override protected PosOrderDto doInBackground() { try { return action.run(); } catch (Exception ex) { error = ex; return null; } }
      @Override protected void done() {
        if (error != null) { showError(actionName + " thất bại: " + error.getMessage()); return; }
        try {
          PosOrderDto order = get();
          loadOrders();
          if (clearDetail) {
            orderDetailPanel.removeAll();
            orderDetailPanel.revalidate();
            orderDetailPanel.repaint();
          } else if (order != null) {
            renderOrderDetail(order, false);
          }
          showInfo(actionName + " thành công");
        } catch (Exception ex) {
          showError("Không cập nhật được giao diện sau khi " + actionName.toLowerCase() + ": " + ex.getMessage());
        }
      }
    }.execute();
  }

  private void switchTab(int idx) {
    activeTab = idx;
    for (JButton button : tabButtons) button.repaint();
    mainContent.removeAll();
    if (idx == 0) {
      mainContent.add(buildCreateOrderPanel(), "0");
      cardLayout.show(mainContent, "0");
    } else {
      mainContent.add(buildOrderActionPanel(false), String.valueOf(idx));
      cardLayout.show(mainContent, String.valueOf(idx));
      orderDetailPanel.removeAll();
      orderDetailPanel.revalidate();
      loadOrders();
    }
    mainContent.revalidate();
    mainContent.repaint();
  }
  private JPanel wrapField(String label, JTextField field) {
    JPanel panel = new JPanel(new BorderLayout(0, 4));
    panel.setOpaque(false);
    JLabel l = new JLabel(label);
    l.setForeground(MUTED);
    l.setFont(UiTheme.regular(12));
    panel.add(l, BorderLayout.NORTH);
    OutlinedInputPanel input = new OutlinedInputPanel();
    input.setLayout(new BorderLayout());
    input.add(field, BorderLayout.CENTER);
    panel.add(input, BorderLayout.CENTER);
    return panel;
  }

  private JPanel wrapCombo(String label, JComboBox<?> combo) {
    JPanel panel = new JPanel(new BorderLayout(0, 4));
    panel.setOpaque(false);
    JLabel l = new JLabel(label);
    l.setForeground(MUTED);
    l.setFont(UiTheme.regular(12));
    styleCombo(combo);
    OutlinedInputPanel input = new OutlinedInputPanel();
    input.setLayout(new BorderLayout());
    input.add(combo, BorderLayout.CENTER);
    panel.add(l, BorderLayout.NORTH);
    panel.add(input, BorderLayout.CENTER);
    return panel;
  }

  private JScrollPane hiddenScrollPane(java.awt.Component component) {
    JScrollPane pane = new JScrollPane(component); pane.setBorder(BorderFactory.createEmptyBorder()); pane.getViewport().setOpaque(false); pane.setOpaque(false);
    pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER); pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    pane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0)); pane.getVerticalScrollBar().setUnitIncrement(18); return pane;
  }
  private boolean isPending(PosOrderDto order) {
    return order != null
        && "PENDING".equalsIgnoreCase(order.getTrangThaiHoaDon())
        && "PENDING".equalsIgnoreCase(order.getTrangThaiThanhToan());
  }

  private boolean isPaid(PosOrderDto order) {
    return order != null
        && ("SUCCESS".equalsIgnoreCase(order.getTrangThaiThanhToan())
            || "COMPLETED".equalsIgnoreCase(order.getTrangThaiHoaDon())
            || "PAID".equalsIgnoreCase(order.getTrangThaiHoaDon()));
  }

  private String paymentSummary(PosOrderDto order) {
    if (isPaid(order)) {
      String method = order.getPhuongThucThanhToan();
      if ("BANK_TRANSFER".equalsIgnoreCase(method)) return "Thanh toán hoàn tất - Chuyển khoản";
      if ("CASH".equalsIgnoreCase(method)) return "Thanh toán hoàn tất - Tiền mặt";
      return "Thanh toán hoàn tất";
    }
    if ("CANCELLED".equalsIgnoreCase(order.getTrangThaiHoaDon())) return "Đơn đã hủy";
    return "Đang chờ thanh toán";
  }

  private void refreshOrderDetail(Long maHoaDon, boolean cancelMode) {
    new SwingWorker<PosOrderDto, Void>() {
      private Exception error;
      @Override protected PosOrderDto doInBackground() { try { return apiClient.getOrder(maHoaDon); } catch (Exception ex) { error = ex; return null; } }
      @Override protected void done() {
        if (error != null) { showError("Không tải lại được đơn #" + maHoaDon + ": " + error.getMessage()); return; }
        try { renderOrderDetail(get(), cancelMode); loadOrders(); } catch (Exception ignored) {}
      }
    }.execute();
  }

  private void printReceipt(PosOrderDto order) {
    new SwingWorker<Path, Void>() {
      private Exception error;

      @Override
      protected Path doInBackground() {
        try {
          byte[] pdfBytes = apiClient.downloadInvoicePdf(order.getMaHoaDon());
          Path dir = Path.of(System.getProperty("user.home"), "Downloads", "hoa-don-phung-loc");
          Files.createDirectories(dir);
          Path file = dir.resolve("hoa-don-" + order.getMaHoaDon() + ".pdf");
          Files.write(file, pdfBytes);
          return file;
        } catch (Exception ex) {
          error = ex;
          return null;
        }
      }

      @Override
      protected void done() {
        if (error != null) {
          showError("Không xuất được PDF hóa đơn: " + error.getMessage());
          return;
        }
        try {
          Path file = get();
          openPdfOrFolder(file);
          showInfo("Đã xuất PDF hóa đơn: " + file);
        } catch (Exception ex) {
          showError("Đã tạo PDF nhưng không mở được file/thư mục: " + ex.getMessage());
        }
      }
    }.execute();
  }

  private void openPdfOrFolder(Path file) throws Exception {
    if (file == null) return;
    try {
      if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().open(file.toFile());
        return;
      }
    } catch (Exception ignored) {
      // May chua gan app mac dinh cho PDF thi dung Windows FileProtocolHandler.
    }

    try {
      new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", file.toString()).start();
      return;
    } catch (Exception ignored) {
      // Fallback cuoi: mo thu muc chua file de nguoi dung thay PDF vua tao.
    }

    Path parent = file.getParent();
    if (parent != null && Desktop.isDesktopSupported()) {
      Desktop.getDesktop().open(parent.toFile());
    }
  }
  private RoundedButton primaryButton(String text) { return new RoundedButton(text).background(ACCENT).hover(ACCENT_DARK).radius(10); }
  private RoundedButton secondaryButton(String text) { RoundedButton b = new RoundedButton(text).background(Color.decode("#B9B9B9")).hover(Color.decode("#A8A8A8")); b.setForeground(TEXT); return b; }
  private RoundedButton dangerButton(String text) { return new RoundedButton(text).background(DANGER).hover(Color.decode("#A83427")); }
  private OptionDto selectedBranch() { return (OptionDto) branchCombo.getSelectedItem(); }
  private OptionDto selectedPos() { return (OptionDto) posCombo.getSelectedItem(); }
  private BigDecimal cartTotal() { BigDecimal total = BigDecimal.ZERO; for (CartLine line : cart.values()) total = total.add(nullToZero(line.product.getGiaBanHienTai()).multiply(BigDecimal.valueOf(line.quantity))); return total; }
  private String formatMoney(BigDecimal value) { return moneyFormat.format(nullToZero(value)) + " VND"; }
  private BigDecimal nullToZero(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
  private String safe(String value) { return value == null ? "" : value; }
  private String text(JTextField field) { return field.getText() == null ? "" : field.getText().trim(); }
  private String ellipsis(String value, int max) { String text = safe(value); return text.length() <= max ? text : text.substring(0, max - 3) + "..."; }
  private String displayOrderStatus(String status) { if ("PENDING".equals(status)) return "Chờ thanh toán"; if ("COMPLETED".equals(status)) return "Hoàn tất"; if ("CANCELLED".equals(status)) return "Đã hủy"; if ("PAID".equals(status)) return "Đã thanh toán"; return safe(status); }
  private String displayPaymentStatus(String status, String method) { String s = "SUCCESS".equals(status) ? "Thành công" : "PENDING".equals(status) ? "Chờ" : "FAILED".equals(status) ? "Thất bại" : safe(status); return method == null || method.isBlank() ? s : s + " - " + method; }
  private void showInfo(String m) { JOptionPane.showMessageDialog(this, m, "POS", JOptionPane.INFORMATION_MESSAGE); }
  private void showWarning(String m) { JOptionPane.showMessageDialog(this, m, "POS", JOptionPane.WARNING_MESSAGE); }
  private void showError(String m) { JOptionPane.showMessageDialog(this, m, "POS", JOptionPane.ERROR_MESSAGE); }
  private DocumentListener doc(Runnable r) { return new DocumentListener() { public void insertUpdate(DocumentEvent e) { r.run(); } public void removeUpdate(DocumentEvent e) { r.run(); } public void changedUpdate(DocumentEvent e) { r.run(); } }; }


  private JTextField createSearchField(String placeholder) {
    IconPlaceholderTextField field =
        new IconPlaceholderTextField(placeholder, new FlatSVGIcon("icons/tim.svg", 18, 18), MUTED, TEXT);
    field.setFont(UiTheme.regular(14));
    field.setForeground(TEXT);
    field.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
    field.setOpaque(false);
    return field;
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

  private void showQrDialog(BankQrDto qr) {
    JDialog dialog = new JDialog(this, "Thanh toán QR", true);
    JPanel panel = new JPanel(new BorderLayout(0, 12));
    panel.setBorder(new EmptyBorder(14, 14, 14, 14));
    panel.setBackground(WHITE);

    JLabel title = new JLabel("Quét QR để thanh toán đơn #" + qr.getMaHoaDon());
    title.setFont(UiTheme.bold(16));
    title.setForeground(ACCENT_DARK);
    panel.add(title, BorderLayout.NORTH);

    BufferedImage qrImage = buildQrImage(qr);
    if (qrImage != null) {
      JLabel qrLabel = new JLabel(new javax.swing.ImageIcon(qrImage));
      qrLabel.setHorizontalAlignment(SwingConstants.CENTER);
      panel.add(qrLabel, BorderLayout.CENTER);
    } else {
      JTextField raw = new JTextField(qr.getQrCode() == null ? "" : qr.getQrCode());
      raw.setEditable(false);
      panel.add(raw, BorderLayout.CENTER);
    }

    JPanel info = new JPanel(new GridLayout(0, 1, 0, 6));
    info.setOpaque(false);
    JLabel state = new JLabel("Đang chờ payOS xác nhận thanh toán...");
    state.setForeground(MUTED);
    info.add(new JLabel("Số tiền: " + formatMoney(qr.getSoTien())));
    info.add(new JLabel("Mã payOS: " + qr.getOrderCode()));
    info.add(state);
    if (qr.getCheckoutUrl() != null && !qr.getCheckoutUrl().isBlank()) {
      JButton openLink = secondaryButton("Mở trang thanh toán payOS");
      openLink.addActionListener(
          e -> {
            try {
              if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(qr.getCheckoutUrl()));
              }
            } catch (Exception ex) {
              showError("Không mở được link payOS: " + ex.getMessage());
            }
          });
      info.add(openLink);
    }
    panel.add(info, BorderLayout.SOUTH);

    final Timer[] timer = new Timer[1];
    timer[0] = new Timer(3000, e -> {
      new SwingWorker<PosOrderDto, Void>() {
        private Exception error;
        @Override protected PosOrderDto doInBackground() { try { return apiClient.getOrder(qr.getMaHoaDon()); } catch (Exception ex) { error = ex; return null; } }
        @Override protected void done() {
          if (error != null) {
            state.setText("Chưa kiểm tra được trạng thái thanh toán.");
            return;
          }
          try {
            PosOrderDto order = get();
            if (isPaid(order)) {
              timer[0].stop();
              state.setText("Thanh toán hoàn tất. Có thể in hóa đơn.");
              state.setForeground(SUCCESS);
              dialog.dispose();
              renderOrderDetail(order, false);
              loadOrders();
            }
          } catch (Exception ignored) {}
        }
      }.execute();
    });
    dialog.setContentPane(panel);
    dialog.pack();
    dialog.setSize(new Dimension(Math.max(360, dialog.getWidth()), dialog.getHeight()));
    dialog.setLocationRelativeTo(this);
    dialog.addWindowListener(new java.awt.event.WindowAdapter() {
      @Override public void windowClosed(java.awt.event.WindowEvent e) { timer[0].stop(); refreshOrderDetail(qr.getMaHoaDon(), false); }
      @Override public void windowClosing(java.awt.event.WindowEvent e) { timer[0].stop(); refreshOrderDetail(qr.getMaHoaDon(), false); }
    });
    timer[0].setInitialDelay(0);
    timer[0].start();
    dialog.setVisible(true);
  }
  private BufferedImage buildQrImage(BankQrDto qr) {
    String value = qr.getQrCode();
    if (value == null || value.isBlank()) {
      value = qr.getCheckoutUrl();
    }
    if (value == null || value.isBlank()) {
      return null;
    }

    try {
      String data = value.trim();
      if (data.startsWith("data:image")) {
        int comma = data.indexOf(',');
        if (comma >= 0) {
          byte[] bytes = Base64.getDecoder().decode(data.substring(comma + 1));
          return ImageIO.read(new ByteArrayInputStream(bytes));
        }
      }
      if (data.length() > 100 && data.matches("^[A-Za-z0-9+/=\\r\\n]+$")) {
        try {
          byte[] bytes = Base64.getDecoder().decode(data.replaceAll("\\s", ""));
          BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
          if (image != null) return image;
        } catch (Exception ignored) {
          // Neu khong phai base64 anh thi render QR tu chuoi thanh toan.
        }
      }
      QRCodeWriter writer = new QRCodeWriter();
      BitMatrix matrix = writer.encode(data, BarcodeFormat.QR_CODE, 260, 260);
      return MatrixToImageWriter.toBufferedImage(matrix);
    } catch (Exception ex) {
      return null;
    }
  }

  @FunctionalInterface private interface OrderAction { PosOrderDto run() throws Exception; }
  private static class StatusOption { final String code; final String name; StatusOption(String code, String name) { this.code = code; this.name = name; } @Override public String toString() { return name; } }
  private static class CartLine { final PosProductDto product; int quantity; CartLine(PosProductDto product) { this.product = product; } }
  private static class RoundedPanel extends JPanel {
    RoundedPanel() { setOpaque(false); }
    @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(WHITE); g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14); g2.setColor(BORDER); g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14); g2.dispose(); super.paintComponent(g); }
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
      g2.setColor(Color.decode("#BFB6AD"));
      g2.setStroke(new BasicStroke(1.2f));
      g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);
      g2.dispose();
    }
  }

  private static class IconPlaceholderTextField extends JTextField {
    private final String placeholder;
    private final javax.swing.Icon icon;
    private final Color placeholderColor;
    private final Color textColor;

    IconPlaceholderTextField(String placeholder, javax.swing.Icon icon, Color placeholderColor, Color textColor) {
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
    public void paintCurrentValueBackground(Graphics g, java.awt.Rectangle bounds, boolean hasFocus) {}
  }

  private static class DesignComboBoxRenderer extends DefaultListCellRenderer {
    @Override
    public java.awt.Component getListCellRendererComponent(
        JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      JLabel label =
          (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      if (value instanceof OptionDto option) {
        label.setText(option.getName());
      }
      if (value instanceof StatusOption option) {
        label.setText(option.name);
      }
      label.setFont(UiTheme.regular(14));
      label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 28));
      label.setForeground(TEXT);
      label.setBackground(isSelected && index >= 0 ? Color.decode("#F8DCC6") : WHITE);
      return label;
    }
  }

  private static class ProductImagePanel extends JPanel {
    private final String url; private final String fallbackText; private BufferedImage image;
    ProductImagePanel(String url, String fallbackText) { this.url = url; this.fallbackText = fallbackText; setOpaque(false); if (url != null && !url.isBlank()) load(); }
    private void load() { new SwingWorker<BufferedImage, Void>() { @Override protected BufferedImage doInBackground() throws Exception { return ImageIO.read(new URL(url)); } @Override protected void done() { try { image = get(); } catch (Exception ignored) {} repaint(); } }.execute(); }
    @Override protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      g2.setColor(Color.decode("#F6E7DA")); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
      if (image != null) { double scale = Math.max(getWidth() / (double) image.getWidth(), getHeight() / (double) image.getHeight()); int w = (int) Math.round(image.getWidth() * scale); int h = (int) Math.round(image.getHeight() * scale); int x = (getWidth() - w) / 2; int y = (getHeight() - h) / 2; Shape old = g2.getClip(); g2.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10)); g2.drawImage(image.getScaledInstance(w, h, Image.SCALE_SMOOTH), x, y, null); g2.setClip(old); }
      else { g2.setColor(MUTED); g2.setFont(UiTheme.regular(12)); FontMetrics fm = g2.getFontMetrics(); String text = fallbackText == null || fallbackText.isBlank() ? "Sản phẩm" : fallbackText; if (text.length() > 18) text = text.substring(0, 15) + "..."; g2.drawString(text, Math.max(8, (getWidth() - fm.stringWidth(text)) / 2), getHeight() / 2 + 4); }
      g2.setColor(BORDER); g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10); g2.dispose();
    }
  }
}



