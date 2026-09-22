package com.back.shared.market.dto;

import java.time.LocalDateTime;

public record OrderItemDto(
        int id,
        LocalDateTime createDate,
        LocalDateTime modifyDate,
        int orderId,
        int buyerId,
        String buyerName,
        int sellerId,
        String sellerName,
        int productId,
        String productName,
        long price,
        long salePrice,
        double payoutRate
) {
}
