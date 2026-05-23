package com.coffeechain.dto.request;

import java.util.List;

public record CreatePosOrderRequest(
    Long maChiNhanh, Long maPos, List<PosOrderItemRequest> items, String ghiChu) {}
