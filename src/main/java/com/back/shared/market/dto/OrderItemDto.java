package com.back.shared.market.dto;

import com.back.shared.modelType.HashModelTypeCode;

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
        double payoutRate,
        long payoutFee,
        long salePriceWithoutFee
) implements HashModelTypeCode {
    @Override
    public String getModelTypeCode() {
        return "OrderItem";
    }
}
