package com.coffeechain.service;

import com.coffeechain.exception.AppException;
import com.coffeechain.repository.PosRepository;
import com.coffeechain.repository.PosRepository.LotRow;
import com.coffeechain.repository.PosRepository.OrderDetailForDeductionRow;
import com.coffeechain.repository.PosRepository.OrderRow;
import com.coffeechain.repository.PosRepository.RecipeLineRow;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PosInventoryDeductionService {
    private final PosRepository posRepository;

    public PosInventoryDeductionService(PosRepository posRepository) {
        this.posRepository = posRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public void completePaidOrderAndDeductStock(Long maHoaDon) {
        OrderRow order = posRepository.lockOrder(maHoaDon)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy hóa đơn"));

        if ("COMPLETED".equals(order.trangThaiHoaDon())) {
            return;
        }

        if (!"SUCCESS".equals(order.trangThaiThanhToan())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Hóa đơn chưa thanh toán thành công");
        }

        if (posRepository.hasSaleDeduction(maHoaDon)) {
            posRepository.markOrderCompleted(maHoaDon);
            return;
        }

        Long maKho = posRepository.findBranchWarehouse(order.maChiNhanh());
        if (maKho == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Chi nhánh chưa có kho bán hàng");
        }

        List<OrderDetailForDeductionRow> orderDetails = posRepository.findOrderDetailsForDeduction(maHoaDon);
        if (orderDetails.isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Hóa đơn không có dòng sản phẩm");
        }

        for (OrderDetailForDeductionRow detail : orderDetails) {
            List<RecipeLineRow> recipeLines = posRepository.findRecipeLines(detail.maSanPham());

            if (recipeLines.isEmpty()) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Sản phẩm chưa có công thức");
            }

            for (RecipeLineRow recipe : recipeLines) {
                BigDecimal requiredQty = recipe.soLuongCan()
                        .multiply(BigDecimal.valueOf(detail.soLuong()));

                deductIngredientByFefo(
                        maHoaDon,
                        detail.maCtHoaDon(),
                        maKho,
                        recipe.maNguyenLieu(),
                        recipe.soLuongCan(),
                        requiredQty,
                        order.maNguoiDung()
                );
            }
        }

        posRepository.markOrderCompleted(maHoaDon);
    }

    private void deductIngredientByFefo(
            Long maHoaDon,
            Long maCtHoaDon,
            Long maKho,
            Long maNguyenLieu,
            BigDecimal soLuongMoiSp,
            BigDecimal requiredQty,
            Long nguoiThaoTac
    ) {
        BigDecimal remaining = requiredQty;
        List<LotRow> lots = posRepository.lockLotsForSale(maKho, maNguyenLieu);

        for (LotRow lot : lots) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal available = lot.soLuongConLai();
            BigDecimal deductQty = available.min(remaining);

            BigDecimal stockBefore = posRepository.lockStock(maKho, maNguyenLieu);
            if (stockBefore.compareTo(deductQty) < 0) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Tồn kho không đủ để bán");
            }

            BigDecimal stockAfter = stockBefore.subtract(deductQty);

            posRepository.decreaseLot(lot.maLoHang(), deductQty);
            posRepository.decreaseStock(maKho, maNguyenLieu, deductQty);

            posRepository.createSaleDeduction(
                    maHoaDon,
                    maCtHoaDon,
                    maKho,
                    maNguyenLieu,
                    lot.maLoHang(),
                    soLuongMoiSp,
                    deductQty
            );

            posRepository.createInventoryJournal(
                    maKho,
                    maNguyenLieu,
                    lot.maLoHang(),
                    maHoaDon,
                    deductQty.negate(),
                    stockBefore,
                    stockAfter,
                    nguoiThaoTac
            );

            remaining = remaining.subtract(deductQty);
        }

        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Không đủ nguyên liệu để hoàn tất hóa đơn");
        }
    }
}