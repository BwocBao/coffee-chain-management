package com.coffeechain.ui;

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
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JComponent;
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
import javax.swing.event.TableModelEvent;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableModel;

import com.coffeechain.service.InventoryApiClient;
import com.coffeechain.service.SessionManager;
import com.coffeechain.service.InventoryApiClient.CreateExportReceiptItemRequest;
import com.coffeechain.service.InventoryApiClient.CreateExportReceiptRequest;
import com.coffeechain.service.InventoryApiClient.ExportLotSelectionRequest;
import com.coffeechain.service.InventoryApiClient.ExportReceiptDto;
import com.coffeechain.service.InventoryApiClient.InventoryExportLookupDto;
import com.coffeechain.service.InventoryApiClient.InventoryLotDto;
import com.coffeechain.service.InventoryApiClient.OptionDto;
import com.coffeechain.ui.common.RoundedButton;
import com.coffeechain.ui.common.RoundedPanel;
import com.coffeechain.ui.common.UiTheme;
import com.formdev.flatlaf.extras.FlatSVGIcon;

/**
 * Màn hình tạo phiếu xuất kho.
 *
 * =========================
 * LUỒNG TỔNG QUAN
 * =========================
 *
 * Khi mở màn hình:
 * XuatKhoFrame()
 *   -> buildHeader()
 *   -> buildReceiptInfo()
 *   -> buildIngredientPicker()
 *   -> buildTables()
 *   -> buildQuickInputRow()
 *   -> buildLotSection()
 *   -> buildNoteAndFooter()
 *   -> bindEvents()
 *   -> loadLookups()
 *
 * Trong đó:
 * - Các hàm build... chỉ dùng để dựng giao diện.
 * - bindEvents() dùng để gắn sự kiện cho combo, checkbox, table.
 * - loadLookups() là API đầu tiên được gọi để lấy dữ liệu kho và loại xuất.
 *
 * =========================
 * LUỒNG API
 * =========================
 *
 * 1. loadLookups()
 *    -> gọi GET /api/inventory/exports/lookups
 *    -> đổ dữ liệu vào warehouseCombo và exportTypeCombo
 *    -> gọi tiếp loadStockForSelectedWarehouse()
 *
 * 2. loadStockForSelectedWarehouse()
 *    -> gọi GET /api/inventory/exports/stock?maKho=...
 *    -> đổ dữ liệu vào bảng nguyên liệu bên trái
 *    -> đổ dữ liệu vào ingredientCombo
 *
 * 3. Nếu bật "Chọn lô thủ công":
 *    refreshLotsIfManual()
 *      -> gọi GET /api/inventory/exports/lots?maKho=...&maNguyenLieu=...
 *      -> đổ dữ liệu vào bảng lô
 *
 * 4. Khi bấm "Lưu phiếu":
 *    saveReceipt()
 *      -> gom dữ liệu từ list lines
 *      -> tạo CreateExportReceiptRequest
 *      -> gọi POST /api/inventory/exports
 *
 * =========================
 * LUỒNG SỰ KIỆN
 * =========================
 *
 * - Chọn kho:
 *   warehouseCombo ActionListener
 *      -> loadStockForSelectedWarehouse()
 *      -> refreshLotsIfManual()
 *
 * - Chọn nguyên liệu:
 *   ingredientCombo ActionListener
 *      -> refreshLotsIfManual()
 *
 * - Tick chọn lô thủ công:
 *   manualLotCheckBox ActionListener
 *      -> handleManualModeChanged()
 *
 * - Gõ số lượng ở bảng lô:
 *   lotTableModel TableModelListener
 *      -> updateManualQuantityFromLots()
 *
 * - Chọn nguyên liệu ở bảng trái:
 *   ingredientTable ListSelectionListener
 *      -> fillSelectedIngredientToDetail()
 *
 * - Bấm "Thêm vào phiếu":
 *   addLine()
 *
 * - Bấm "Lưu phiếu":
 *   saveReceipt()
 *
 * Lưu ý:
 * - lines là danh sách dữ liệu thật sẽ gửi backend.
 * - exportTableModel chỉ là dữ liệu hiển thị cho người dùng xem.
 * - SwingWorker được dùng để gọi API ở background, tránh làm đứng giao diện Swing.
 */
public class XuatKhoFrame extends JFrame {

    private static final int ROOT_W = 1440;
    private static final int ROOT_H = 960;
    private static final int VIEW_H = 800;
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
    private final JComboBox<OptionDto> exportTypeCombo = new JComboBox<>();
    private final JComboBox<OptionDto> ingredientCombo = new JComboBox<>();
    private final JCheckBox manualLotCheckBox = new JCheckBox("Chọn lô thủ công");

    private final JTextField createdDateField = new JTextField(LocalDate.now().toString());
    private final JTextField creatorField = new JTextField(SessionManager.getCurrentUserDisplayName());
    private final JTextField quantityField = new JTextField();
    private final JTextField priceField = new JTextField();
    private final JTextArea noteArea = new JTextArea();

    private final JLabel totalLabel = new JLabel("0");
    private final JLabel statusLabel = new JLabel("Đang tải dữ liệu...");
    private final RoundedButton saveButton = primaryButton("Lưu phiếu");

    private final DefaultTableModel ingredientTableModel = new DefaultTableModel(
            new Object[]{"Mã NL", "Tên nguyên liệu", "Đơn vị tính", "Tồn kho"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable ingredientTable = new JTable(ingredientTableModel);

    private final DefaultTableModel exportTableModel = new DefaultTableModel(
            new Object[]{"Mã NL", "Tên nguyên liệu", "Lô", "Số lượng", "Đơn giá", "Thành tiền"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable exportTable = new JTable(exportTableModel);

    private final DefaultTableModel lotTableModel = new DefaultTableModel(
            new Object[]{"Mã lô", "Hạn sử dụng", "Còn lại", "SL xuất"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 3;
        }
    };
    private final JTable lotTable = new JTable(lotTableModel);

    private List<InventoryApiClient.InventoryStockDto> allIngredients = new ArrayList<>();
    private List<InventoryLotDto> currentLots = new ArrayList<>();
    private final List<ExportLine> lines = new ArrayList<>();
    private final DecimalFormat numberFormat = new DecimalFormat("#,##0.###");

    public XuatKhoFrame() {
        setTitle("Phụng Lộc - Xuất kho");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        root.setPreferredSize(new Dimension(ROOT_W, ROOT_H));
        root.setBackground(WHITE);

        JScrollPane pageScroll = createHiddenPageScrollPane(root);
        setContentPane(pageScroll);

        buildHeader();
        buildReceiptInfo();
        buildIngredientPicker();
        buildTables();
        buildQuickInputRow();
        buildLotSection();
        buildNoteAndFooter();
        bindEvents();
        loadLookups();

        pack();
        setLocationRelativeTo(null);
    }

    private void buildHeader() {
        JLabel title = new JLabel("XUẤT KHO");
        title.setBounds(44, 22, 420, 40);
        title.setForeground(PRIMARY);
        title.setFont(UiTheme.bold(32));
        root.add(title);

        RoundedButton backButton = primaryButton("Quay lại");
        FlatSVGIcon backIcon = new FlatSVGIcon("icons/nhap-kho/left.svg", 16, 18);
        backButton.setIcon(backIcon);
        backButton.setIconTextGap(8);
        backButton.setHorizontalAlignment(SwingConstants.CENTER);
        backButton.setBounds(1220, 30, 110, 34);
        backButton.addActionListener(e -> {
            new KhoMenuFrame().setVisible(true);
            dispose();
        });
        root.add(backButton);
    }

    private void buildReceiptInfo() {
        JLabel filterTitle = new JLabel("Phiếu xuất");
        filterTitle.setBounds(44, 76, 200, 24);
        filterTitle.setForeground(TEXT);
        filterTitle.setFont(UiTheme.bold(16));
        root.add(filterTitle);

        RoundedPanel card = new RoundedPanel(16, WHITE, SOFT_BORDER);
        card.setLayout(null);
        card.setBounds(44, 104, 1352, 88);
        root.add(card);

        addLabel(card, "Kho xuất:", 20, 12, 100, 18);
        addCombo(card, warehouseCombo, 20, 34, 250, 34);
        warehouseCombo.setRenderer(new WarehouseComboRenderer());

        addLabel(card, "Loại xuất:", 310, 12, 100, 18);
        addCombo(card, exportTypeCombo, 310, 34, 250, 34);
        exportTypeCombo.setRenderer(new ExportTypeComboRenderer());

        addLabel(card, "Ngày xuất:", 600, 12, 100, 18);
        addField(card, createdDateField, 600, 34, 220, 34, false, LocalDate.now().toString());

        addLabel(card, "Người tạo:", 860, 12, 100, 18);
        addField(card, creatorField, 860, 34, 220, 34, false, SessionManager.getCurrentUserDisplayName());

        manualLotCheckBox.setBounds(1110, 38, 190, 24);
        manualLotCheckBox.setOpaque(false);
        manualLotCheckBox.setForeground(TEXT);
        manualLotCheckBox.setFont(UiTheme.regular(13));
        manualLotCheckBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.add(manualLotCheckBox);
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
        searchField.getDocument().addDocumentListener(new DocumentListener() {
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
        ingredientTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        ingredientTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        ingredientTable.getColumnModel().getColumn(1).setPreferredWidth(210);
        ingredientTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        ingredientTable.getColumnModel().getColumn(3).setPreferredWidth(90);

        ingredientTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        ingredientTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillSelectedIngredientToDetail();
            }
        });

        JScrollPane leftScroll = hiddenScrollPane(ingredientTable);
        leftScroll.setBounds(44, 292, 420, 170);
        root.add(leftScroll);

        JLabel rightTitle = new JLabel("NGUYÊN LIỆU SẼ XUẤT");
        rightTitle.setBounds(540, 262, 400, 24);
        rightTitle.setForeground(TEXT);
        rightTitle.setFont(UiTheme.bold(14));
        root.add(rightTitle);

        configureTable(exportTable);
        exportTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        exportTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    removeSelectedExportLine();
                }
            }
        });

        JScrollPane rightScroll = hiddenScrollPane(exportTable);
        rightScroll.setBounds(540, 292, 856, 170);
        root.add(rightScroll);
    }

    private void buildQuickInputRow() {
        JLabel detailTitle = new JLabel("CHI TIẾT DÒNG XUẤT");
        detailTitle.setBounds(44, 476, 400, 24);
        detailTitle.setForeground(TEXT);
        detailTitle.setFont(UiTheme.bold(14));
        root.add(detailTitle);

        RoundedPanel detailCard = new RoundedPanel(12, WHITE, SOFT_BORDER);
        detailCard.setBounds(44, 506, 1352, 94);
        detailCard.setLayout(null);
        root.add(detailCard);

        addLabel(detailCard, "Nguyên liệu:", 20, 12, 120, 18);
        addCombo(detailCard, ingredientCombo, 20, 34, 250, 30);

        addLabel(detailCard, "Số lượng xuất:", 330, 12, 120, 18);
        addField(detailCard, quantityField, 330, 34, 140, 30, true, "0");

        addLabel(detailCard, "Đơn giá xuất:", 530, 12, 120, 18);
        addField(detailCard, priceField, 530, 34, 140, 30, true, "0");

        RoundedButton addToListButton = primaryButton("Thêm vào phiếu");
        addToListButton.setBounds(1130, 34, 150, 30);
        addToListButton.addActionListener(e -> addLine());
        detailCard.add(addToListButton);
    }

    private void buildLotSection() {
        JLabel lotTitle = new JLabel("LÔ HÀNG CÒN TỒN - CHỈ DÙNG KHI CHỌN LÔ THỦ CÔNG");
        lotTitle.setBounds(44, 612, 600, 24);
        lotTitle.setForeground(TEXT);
        lotTitle.setFont(UiTheme.bold(14));
        root.add(lotTitle);

        configureTable(lotTable);
        lotTable.setEnabled(false);
        lotTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        lotTable.getColumnModel().getColumn(1).setPreferredWidth(130);
        lotTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        lotTable.getColumnModel().getColumn(3).setPreferredWidth(120);

        JScrollPane lotScroll = hiddenScrollPane(lotTable);
        lotScroll.setBounds(44, 642, 1352, 150);
        root.add(lotScroll);
    }

    private void buildNoteAndFooter() {
        JLabel noteLabel = new JLabel("Ghi chú phiếu");
        noteLabel.setBounds(44, 812, 240, 20);
        noteLabel.setForeground(UiTheme.TEXT_DARK);
        noteLabel.setFont(UiTheme.regular(12));
        root.add(noteLabel);

        RoundedPanel notePanel = new RoundedPanel(12, WHITE, SOFT_BORDER);
        notePanel.setBounds(44, 836, 904, 58);
        notePanel.setLayout(null);
        root.add(notePanel);

        noteArea.setFont(UiTheme.regular(13));
        noteArea.setForeground(TEXT);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        noteArea.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        JScrollPane noteScroll = new JScrollPane(noteArea);
        noteScroll.setBounds(1, 1, 902, 56);
        noteScroll.setBorder(null);
        hideScrollBarsButKeepWheel(noteScroll);
        notePanel.add(noteScroll);

        JLabel totalTextLabel = new JLabel("Tổng giá trị:");
        totalTextLabel.setBounds(1030, 812, 120, 28);
        totalTextLabel.setForeground(MUTED);
        totalTextLabel.setFont(UiTheme.regular(14));
        root.add(totalTextLabel);

        totalLabel.setBounds(1120, 812, 260, 28);
        totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        totalLabel.setForeground(PRIMARY_DARK);
        totalLabel.setFont(UiTheme.bold(22));
        root.add(totalLabel);

        saveButton.setBounds(1030, 852, 125, 34);
        saveButton.addActionListener(e -> saveReceipt());
        root.add(saveButton);

        RoundedButton cancelButton = secondaryButton("Hủy");
        cancelButton.setBounds(1179, 852, 96, 34);
        cancelButton.addActionListener(e -> {
            new KhoMenuFrame().setVisible(true);
            dispose();
        });
        root.add(cancelButton);
    }

    /**
     * Gắn toàn bộ sự kiện chính của màn hình.
     *
     * Đây là hàm quan trọng nhất để hiểu UI phản ứng ra sao.
     *
     * 1. warehouseCombo:
     *    Khi người dùng đổi kho xuất:
     *    - loadStockForSelectedWarehouse() để load lại tồn kho của kho mới.
     *    - refreshLotsIfManual() nếu đang bật chọn lô thủ công.
     *
     * 2. ingredientCombo:
     *    Khi người dùng đổi nguyên liệu:
     *    - refreshLotsIfManual() nếu đang bật chọn lô thủ công.
     *
     * 3. manualLotCheckBox:
     *    Khi tick/bỏ tick chọn lô thủ công:
     *    - handleManualModeChanged() xử lý bật/tắt chế độ thủ công.
     *
     * 4. lotTableModel:
     *    Khi người dùng nhập số lượng ở cột "SL xuất":
     *    - updateManualQuantityFromLots() tự cộng tổng số lượng từ các lô.
     */
    private void bindEvents() {
        warehouseCombo.addActionListener(e -> {
            loadStockForSelectedWarehouse();
            refreshLotsIfManual();
        });

        ingredientCombo.addActionListener(e -> refreshLotsIfManual());
        manualLotCheckBox.addActionListener(e -> handleManualModeChanged());

        lotTableModel.addTableModelListener(e -> {
            if (manualLotCheckBox.isSelected()
                    && e.getType() == TableModelEvent.UPDATE
                    && e.getColumn() == 3) {
                updateManualQuantityFromLots();
            }
        });
    }

    /**
     * Load dữ liệu ban đầu cho màn hình xuất kho.
     *
     * API gọi:
     * GET /api/inventory/exports/lookups
     *
     * Dữ liệu nhận:
     * - Danh sách kho xuất.
     * - Danh sách loại xuất.
     *
     * Sau khi load xong:
     * - Set dữ liệu cho warehouseCombo.
     * - Set dữ liệu cho exportTypeCombo.
     * - Gọi loadStockForSelectedWarehouse() để load nguyên liệu còn tồn của kho đang chọn.
     *
     * Vì gọi API có thể mất thời gian, dùng SwingWorker:
     * - doInBackground(): chạy API ở background thread.
     * - done(): cập nhật UI sau khi API chạy xong.
     */
    private void loadLookups() {
        setFormEnabled(false);
        statusLabel.setText("Đang tải dữ liệu...");

        new SwingWorker<InventoryExportLookupDto, Void>() {
            @Override
            protected InventoryExportLookupDto doInBackground() throws Exception {
                return apiClient.getExportLookups();
            }

            @Override
            protected void done() {
                try {
                    InventoryExportLookupDto lookup = get();
                    warehouseCombo.setModel(new DefaultComboBoxModel<>(
                            lookup.getWarehouses().toArray(new OptionDto[0])
                    ));
                    exportTypeCombo.setModel(new DefaultComboBoxModel<>(
                            lookup.getExportTypes().toArray(new OptionDto[0])
                    ));

                    loadStockForSelectedWarehouse();

                    statusLabel.setText("Đã tải dữ liệu. Sẵn sàng tạo phiếu xuất.");
                    setFormEnabled(true);
                } catch (Exception ex) {
                    statusLabel.setText("Không tải được dữ liệu xuất kho");
                    JOptionPane.showMessageDialog(thisFrame(), unwrapMessage(ex), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    /**
     * Xử lý khi người dùng bật/tắt "Chọn lô thủ công".
     *
     * Nếu đã có dòng xuất trong phiếu:
     * - Hỏi người dùng có chắc muốn đổi chế độ không.
     * - Nếu đồng ý thì resetReceipt() để tránh dữ liệu cũ bị lệch.
     *
     * Khi bật chọn lô thủ công:
     * - Bật bảng lotTable.
     * - Khóa quantityField vì số lượng sẽ được cộng từ bảng lô.
     * - Gọi refreshLotsIfManual() để load lô còn tồn.
     *
     * Khi tắt chọn lô thủ công:
     * - Tắt bảng lotTable.
     * - Mở lại quantityField để người dùng tự nhập số lượng tổng.
     */
    private void handleManualModeChanged() {
        if (!lines.isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Đổi chế độ chọn lô sẽ xóa các dòng đã thêm vào phiếu. Bạn muốn tiếp tục?",
                    "Đổi chế độ lô",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) {
                manualLotCheckBox.setSelected(!manualLotCheckBox.isSelected());
                return;
            }
            resetReceipt();
        }

        boolean manual = manualLotCheckBox.isSelected();

        lotTable.setEnabled(manual);
        lotTableModel.setRowCount(0);
        currentLots = new ArrayList<>();

        quantityField.setText("");
        quantityField.setEditable(!manual);
        quantityField.setFocusable(!manual);
        quantityField.setForeground(manual ? MUTED : TEXT);
        quantityField.setToolTipText(manual
                ? "Số lượng xuất được tự động tính từ bảng lô bên dưới"
                : "0");

        if (manual) {
            refreshLotsIfManual();
            statusLabel.setText("Đang dùng chế độ chọn lô thủ công.");
        } else {
            statusLabel.setText("Đang dùng chế độ FEFO tự động.");
        }
    }

    /**
     * Load danh sách lô còn tồn khi đang bật chế độ chọn lô thủ công.
     *
     * API gọi:
     * GET /api/inventory/exports/lots?maKho=...&maNguyenLieu=...
     *
     * Điều kiện chạy:
     * - manualLotCheckBox phải đang được tick.
     * - Đã chọn kho xuất.
     * - Đã chọn nguyên liệu.
     *
     * Sau khi API trả về:
     * - currentLots lưu danh sách lô gốc.
     * - populateLotTable(currentLots) đổ dữ liệu lên bảng lô.
     *
     * currentLots rất quan trọng:
     * - Dòng i trên bảng lotTable tương ứng với currentLots.get(i).
     * - Khi người dùng nhập SL xuất theo lô, mình dựa vào currentLots để biết maLoHang.
     */
    private void refreshLotsIfManual() {
        if (!manualLotCheckBox.isSelected()) {
            return;
        }

        OptionDto warehouse = (OptionDto) warehouseCombo.getSelectedItem();
        OptionDto ingredient = (OptionDto) ingredientCombo.getSelectedItem();
        if (warehouse == null || ingredient == null) {
            return;
        }

        lotTableModel.setRowCount(0);
        quantityField.setText("");
        statusLabel.setText("Đang tải lô còn tồn...");

        new SwingWorker<List<InventoryLotDto>, Void>() {
            @Override
            protected List<InventoryLotDto> doInBackground() throws Exception {
                return apiClient.getExportLots(warehouse.getId(), ingredient.getId());
            }

            @Override
            protected void done() {
                try {
                    currentLots = get();
                    populateLotTable(currentLots);
                    statusLabel.setText("Đã tải " + currentLots.size() + " lô còn tồn.");
                } catch (Exception ex) {
                    statusLabel.setText("Không tải được danh sách lô");
                    JOptionPane.showMessageDialog(thisFrame(), unwrapMessage(ex), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void populateLotTable(List<InventoryLotDto> lots) {
        lotTableModel.setRowCount(0);
        for (InventoryLotDto lot : lots) {
            lotTableModel.addRow(new Object[]{
                    lot.getMaLoHang(),
                    lot.getHanSuDung() == null ? "" : lot.getHanSuDung(),
                    numberFormat.format(nullToZero(lot.getSoLuongConLai())),
                    ""
            });
        }
        updateManualQuantityFromLots();
    }

    /**
     * Thêm một dòng nguyên liệu vào phiếu xuất.
     *
     * Hàm này được gọi khi bấm nút "Thêm vào phiếu".
     *
     * Luồng xử lý:
     * 1. Lấy nguyên liệu đang chọn trong ingredientCombo.
     * 2. Kiểm tra đang ở chế độ FEFO hay chọn lô thủ công.
     *
     * Nếu FEFO:
     * - Đọc số lượng từ quantityField.
     * - Không gửi danh sách lô.
     * - Backend sẽ tự chọn lô theo FEFO.
     *
     * Nếu chọn lô thủ công:
     * - Gọi collectManualLotSelections().
     * - Lấy danh sách lô người dùng nhập.
     * - Tự tính tổng số lượng từ các lô.
     *
     * 3. Đọc đơn giá xuất.
     * 4. Tạo ExportLine.
     * 5. Thêm ExportLine vào list lines.
     * 6. Thêm một dòng hiển thị vào exportTableModel.
     * 7. Cập nhật tổng tiền bằng updateTotal().
     */
    private void addLine() {
        OptionDto ingredient = (OptionDto) ingredientCombo.getSelectedItem();
        if (ingredient == null) {
            showWarning("Vui lòng chọn nguyên liệu");
            return;
        }

        boolean manual = manualLotCheckBox.isSelected();

        BigDecimal quantity;
        List<ExportLotSelectionRequest> lotSelections = new ArrayList<>();

        if (manual) {
            ManualLotResult manualResult = collectManualLotSelections();
            if (manualResult == null) {
                return;
            }
            quantity = manualResult.totalQuantity();
            lotSelections = manualResult.selections();
            quantityField.setText(numberFormat.format(quantity));
        } else {
            quantity = parsePositiveDecimal(quantityField.getText(), "Số lượng xuất phải lớn hơn 0");
            if (quantity == null) {
                return;
            }
        }

        BigDecimal price = parseNonNegativeDecimal(priceField.getText(), "Đơn giá xuất không hợp lệ");
        if (price == null) {
            return;
        }

        ExportLine line = new ExportLine(
                ingredient,
                quantity,
                price,
                manual ? "Thủ công" : "FEFO",
                lotSelections
        );

        lines.add(line);

        exportTableModel.addRow(new Object[]{
                ingredient.getId(),
                ingredient.getName(),
                line.lotMode(),
                numberFormat.format(quantity),
                numberFormat.format(price),
                numberFormat.format(line.amount())
        });

        updateTotal();
        clearLineInputs();

        if (manualLotCheckBox.isSelected()) {
            refreshLotsIfManual();
        }
    }

    /**
     * Thu thập dữ liệu lô do người dùng nhập thủ công.
     *
     * Hàm này chỉ được gọi khi manualLotCheckBox đang bật.
     *
     * Kiểm tra:
     * - Bảng lô có dữ liệu không.
     * - Người dùng có nhập SL xuất cho ít nhất một lô không.
     * - SL xuất theo từng lô phải > 0.
     * - SL xuất không được vượt quá số lượng còn lại của lô.
     *
     * Kết quả trả về:
     * - selections: danh sách maLoHang + soLuongXuat để gửi backend.
     * - totalQuantity: tổng số lượng xuất từ các lô.
     *
     * Nếu dữ liệu không hợp lệ:
     * - Hiện cảnh báo.
     * - Return null để addLine() dừng lại.
     */
    private ManualLotResult collectManualLotSelections() {
        if (lotTable.isEditing()) {
            lotTable.getCellEditor().stopCellEditing();
        }

        if (lotTableModel.getRowCount() == 0) {
            showWarning("Không có lô còn tồn để chọn");
            return null;
        }

        List<ExportLotSelectionRequest> selections = new ArrayList<>();
        BigDecimal selectedTotal = BigDecimal.ZERO;

        for (int i = 0; i < lotTableModel.getRowCount(); i++) {
            Object rawQty = lotTableModel.getValueAt(i, 3);

            if (rawQty == null || rawQty.toString().trim().isEmpty()) {
                continue;
            }

            BigDecimal lotQty = parsePositiveDecimal(
                    rawQty.toString(),
                    "Số lượng xuất theo lô phải lớn hơn 0"
            );

            if (lotQty == null) {
                return null;
            }

            if (i >= currentLots.size()) {
                showWarning("Không xác định được dữ liệu lô ở dòng " + (i + 1));
                return null;
            }

            InventoryLotDto lot = currentLots.get(i);
            BigDecimal available = nullToZero(lot.getSoLuongConLai());

            if (lotQty.compareTo(available) > 0) {
                showWarning("Số lượng xuất vượt quá số lượng còn lại của lô "
                        + lot.getMaLoHang()
                        + ". Còn lại: "
                        + numberFormat.format(available));
                return null;
            }

            ExportLotSelectionRequest selection = new ExportLotSelectionRequest();
            selection.setMaLoHang(lot.getMaLoHang());
            selection.setSoLuongXuat(lotQty);

            selections.add(selection);
            selectedTotal = selectedTotal.add(lotQty);
        }

        if (selections.isEmpty()) {
            showWarning("Vui lòng nhập số lượng xuất cho ít nhất một lô");
            return null;
        }

        return new ManualLotResult(selections, selectedTotal);
    }

    /**
     * Tự cộng tổng số lượng xuất từ cột "SL xuất" trong bảng lô.
     *
     * Hàm này được gọi tự động bởi TableModelListener trong bindEvents().
     *
     * Ví dụ:
     * - Lô 1 nhập 100
     * - Lô 2 nhập 200
     * => quantityField sẽ tự hiện 300
     *
     * Lưu ý:
     * - Hàm này chỉ cập nhật giao diện tạm thời.
     * - Validate chính thức vẫn nằm trong collectManualLotSelections().
     */
    private void updateManualQuantityFromLots() {
        if (!manualLotCheckBox.isSelected()) {
            return;
        }

        BigDecimal total = BigDecimal.ZERO;

        for (int i = 0; i < lotTableModel.getRowCount(); i++) {
            Object rawQty = lotTableModel.getValueAt(i, 3);
            if (rawQty == null || rawQty.toString().trim().isEmpty()) {
                continue;
            }

            try {
                BigDecimal qty = new BigDecimal(rawQty.toString().trim().replace(",", ""));
                if (qty.compareTo(BigDecimal.ZERO) > 0) {
                    total = total.add(qty);
                }
            } catch (NumberFormatException ignored) {
                // Nếu người dùng đang gõ dở hoặc nhập sai, bỏ qua cập nhật tạm thời.
                // Khi bấm "Thêm vào phiếu", collectManualLotSelections() sẽ validate chính thức.
            }
        }

        quantityField.setText(total.compareTo(BigDecimal.ZERO) == 0 ? "" : numberFormat.format(total));
    }

    private void removeSelectedExportLine() {
        int selectedRow = exportTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn muốn xóa dòng nguyên liệu này khỏi phiếu xuất?",
                "Xóa dòng xuất",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        lines.remove(selectedRow);
        exportTableModel.removeRow(selectedRow);
        updateTotal();
    }

    /**
     * Lưu phiếu xuất xuống backend.
     *
     * Hàm này được gọi khi bấm nút "Lưu phiếu".
     *
     * Luồng xử lý:
     * 1. Lấy kho xuất và loại xuất đang chọn.
     * 2. Kiểm tra phiếu có ít nhất một dòng trong list lines.
     * 3. Tạo CreateExportReceiptRequest.
     * 4. Duyệt list lines để tạo danh sách CreateExportReceiptItemRequest.
     *
     * Nếu đang chọn lô thủ công:
     * - Mỗi item sẽ gửi thêm loHangXuat.
     *
     * Nếu FEFO:
     * - Không gửi loHangXuat.
     * - Backend tự chọn lô.
     *
     * 5. Gọi API POST /api/inventory/exports bằng SwingWorker.
     * 6. Thành công:
     *    - Hiện thông báo.
     *    - resetReceipt().
     *    - Nếu đang chọn lô thủ công thì refreshLotsIfManual().
     * 7. Thất bại:
     *    - Hiện lỗi backend trả về.
     */
    private void saveReceipt() {
        OptionDto warehouse = (OptionDto) warehouseCombo.getSelectedItem();
        OptionDto exportType = (OptionDto) exportTypeCombo.getSelectedItem();

        if (warehouse == null || exportType == null) {
            showWarning("Vui lòng chọn kho xuất và loại xuất");
            return;
        }
        if (lines.isEmpty()) {
            showWarning("Vui lòng thêm ít nhất một dòng nguyên liệu");
            return;
        }

        boolean manual = manualLotCheckBox.isSelected();

        CreateExportReceiptRequest request = new CreateExportReceiptRequest();
        request.setMaKho(warehouse.getId());
        request.setLoaiXuat(exportType.getName());
        request.setChonLoThuCong(manual);
        request.setGhiChu(noteArea.getText().trim());

        List<CreateExportReceiptItemRequest> items = new ArrayList<>();
        for (ExportLine line : lines) {
            CreateExportReceiptItemRequest item = new CreateExportReceiptItemRequest();
            item.setMaNguyenLieu(line.ingredient().getId());
            item.setSoLuongXuat(line.quantity());
            item.setDonGiaXuat(line.price());
            if (manual) {
                item.setLoHangXuat(line.lotSelections());
            }
            items.add(item);
        }
        request.setItems(items);

        saveButton.setEnabled(false);
        statusLabel.setText("Đang lưu phiếu xuất...");
        new SwingWorker<ExportReceiptDto, Void>() {
            @Override
            protected ExportReceiptDto doInBackground() throws Exception {
                return apiClient.createExportReceipt(request);
            }

            @Override
            protected void done() {
                saveButton.setEnabled(true);
                try {
                    ExportReceiptDto receipt = get();
                    statusLabel.setText("Đã lưu phiếu xuất #" + receipt.getMaPhieuXuat());
                    JOptionPane.showMessageDialog(
                            thisFrame(),
                            "Đã tạo phiếu xuất #" + receipt.getMaPhieuXuat()
                                    + "\nKho: " + receipt.getTenKho()
                                    + "\nLoại xuất: " + receipt.getLoaiXuat()
                                    + "\nTổng giá trị: " + numberFormat.format(nullToZero(receipt.getTongGiaTriXuat())),
                            "Xuất kho",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    resetReceipt();
                    if (manualLotCheckBox.isSelected()) {
                        refreshLotsIfManual();
                    }
                } catch (Exception ex) {
                    statusLabel.setText("Lưu phiếu xuất thất bại");
                    JOptionPane.showMessageDialog(thisFrame(), unwrapMessage(ex), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void resetReceipt() {
        lines.clear();
        exportTableModel.setRowCount(0);
        noteArea.setText("");
        clearLineInputs();
        updateTotal();
    }

    private void updateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ExportLine line : lines) {
            total = total.add(line.amount());
        }
        totalLabel.setText(numberFormat.format(total));
    }

    private void clearLineInputs() {
        quantityField.setText("");
        priceField.setText("");

        boolean manual = manualLotCheckBox.isSelected();
        quantityField.setEditable(!manual);
        quantityField.setFocusable(!manual);
        quantityField.setForeground(manual ? MUTED : TEXT);
        quantityField.setToolTipText(manual
                ? "Số lượng xuất được tự động tính từ bảng lô bên dưới"
                : "0");

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

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private void setFormEnabled(boolean enabled) {
        warehouseCombo.setEnabled(enabled);
        exportTypeCombo.setEnabled(enabled);
        ingredientCombo.setEnabled(enabled);
        manualLotCheckBox.setEnabled(enabled);
        quantityField.setEnabled(enabled);
        priceField.setEnabled(enabled);
        noteArea.setEnabled(enabled);
        saveButton.setEnabled(enabled);
        lotTable.setEnabled(enabled && manualLotCheckBox.isSelected());

        if (enabled) {
            boolean manual = manualLotCheckBox.isSelected();
            quantityField.setEditable(!manual);
            quantityField.setFocusable(!manual);
            quantityField.setForeground(manual ? MUTED : TEXT);
        }
    }

    private void populateIngredientTable(List<InventoryApiClient.InventoryStockDto> ingredients) {
        ingredientTableModel.setRowCount(0);

        for (InventoryApiClient.InventoryStockDto ingredient : ingredients) {
            ingredientTableModel.addRow(new Object[]{
                    ingredient.getId(),
                    ingredient.getName(),
                    getUnitText(ingredient),
                    numberFormat.format(nullToZero(ingredient.getSoLuongTon()))
            });
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

        List<InventoryApiClient.InventoryStockDto> filtered = new ArrayList<>();

        for (InventoryApiClient.InventoryStockDto ingredient : allIngredients) {
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
                if (manualLotCheckBox.isSelected()) {
                    refreshLotsIfManual();
                } else {
                    quantityField.requestFocusInWindow();
                }
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

    private JTextField createSearchField() {
        Icon searchIcon = new FlatSVGIcon("icons/tim.svg", 18, 18);
        JTextField field = new IconPlaceholderTextField(SEARCH_PLACEHOLDER, searchIcon, MUTED, TEXT);
        styleField(field, SEARCH_PLACEHOLDER);
        return field;
    }

    /**
     * Load danh sách nguyên liệu còn tồn theo kho đang chọn.
     *
     * API gọi:
     * GET /api/inventory/exports/stock?maKho=...
     *
     * Hàm này được gọi trong 2 trường hợp:
     * 1. Sau khi loadLookups() xong.
     * 2. Khi người dùng đổi kho trong warehouseCombo.
     *
     * Sau khi API trả về:
     * - allIngredients lưu toàn bộ nguyên liệu còn tồn.
     * - ingredientCombo được cập nhật để chọn nguyên liệu.
     * - ingredientTable bên trái được cập nhật để hiển thị danh sách nguyên liệu.
     */
    private void loadStockForSelectedWarehouse() {
        OptionDto warehouse = (OptionDto) warehouseCombo.getSelectedItem();
        if (warehouse == null) {
            return;
        }

        ingredientTableModel.setRowCount(0);
        ingredientCombo.setModel(new DefaultComboBoxModel<>());
        statusLabel.setText("Đang tải tồn kho...");

        new SwingWorker<List<InventoryApiClient.InventoryStockDto>, Void>() {
            @Override
            protected List<InventoryApiClient.InventoryStockDto> doInBackground() throws Exception {
                return apiClient.getExportStock(warehouse.getId());
            }

            @Override
            protected void done() {
                try {
                    allIngredients = get();

                    ingredientCombo.setModel(new DefaultComboBoxModel<>(
                            allIngredients.toArray(new OptionDto[0])
                    ));

                    populateIngredientTable(allIngredients);
                    statusLabel.setText("Đã tải " + allIngredients.size() + " nguyên liệu còn tồn.");
                } catch (Exception ex) {
                    statusLabel.setText("Không tải được tồn kho");
                    JOptionPane.showMessageDialog(thisFrame(), unwrapMessage(ex), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
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
        return new RoundedButton(text)
                .background(PRIMARY)
                .hover(PRIMARY_DARK)
                .radius(10);
    }

    private static RoundedButton secondaryButton(String text) {
        RoundedButton button = new RoundedButton(text)
                .background(Color.decode("#B9B9B9"))
                .hover(Color.decode("#A8A8A8"))
                .radius(10);
        button.setForeground(TEXT);
        return button;
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Xuất kho", JOptionPane.WARNING_MESSAGE);
    }

    private JFrame thisFrame() {
        return this;
    }

    private String unwrapMessage(Exception ex) {
        Throwable current = ex;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? "Không xử lý được yêu cầu" : current.getMessage();
    }

    /**
     * Một dòng nguyên liệu đã được thêm vào phiếu xuất.
     *
     * Đây là dữ liệu thật dùng để gửi backend.
     *
     * ingredient:
     * - nguyên liệu được chọn.
     *
     * quantity:
     * - tổng số lượng xuất.
     *
     * price:
     * - đơn giá xuất.
     *
     * lotMode:
     * - chỉ dùng để hiển thị trên bảng, ví dụ "FEFO" hoặc "Thủ công".
     *
     * lotSelections:
     * - danh sách lô cụ thể khi chọn lô thủ công.
     * - nếu FEFO thì danh sách này rỗng.
     */
    private record ExportLine(
            OptionDto ingredient,
            BigDecimal quantity,
            BigDecimal price,
            String lotMode,
            List<ExportLotSelectionRequest> lotSelections
    ) {
        BigDecimal amount() {
            return quantity.multiply(price);
        }
    }

    private record ManualLotResult(
            List<ExportLotSelectionRequest> selections,
            BigDecimal totalQuantity
    ) {}

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
            // OutlinedInputPanel đã vẽ nền và viền.
        }
    }

    private static class DesignComboBoxRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list,
                    value,
                    index,
                    isSelected,
                    cellHasFocus
            );

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

    private static class WarehouseComboRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list,
                    value,
                    index,
                    isSelected,
                    cellHasFocus
            );

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
    private static String exportTypeDisplayText(OptionDto option) {
        if (option == null) {
            return "";
        }

        String code = option.getName();
        if ("INTERNAL_USE".equals(code)) {
            return "Xuất dùng nội bộ";
        }
        if ("RETURN_SUPPLIER".equals(code)) {
            return "Trả nhà cung cấp";
        }
        if ("TRAINING".equals(code)) {
            return "Xuất đào tạo";
        }
        if ("OTHER".equals(code)) {
            return "Xuất khác";
        }

        String description = option.getDescription();
        if (description != null && !description.isBlank()) {
            return description;
        }
        return code == null ? "" : code;
    }

    private static class ExportTypeComboRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list,
                    value,
                    index,
                    isSelected,
                    cellHasFocus
            );

            if (value instanceof OptionDto option) {
                label.setText(exportTypeDisplayText(option));
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

    private static class IconPlaceholderTextField extends JTextField {
        private final String placeholder;
        private final Icon icon;
        private final Color placeholderColor;
        private final Color textColor;

        IconPlaceholderTextField(String placeholder, Icon icon, Color placeholderColor, Color textColor) {
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