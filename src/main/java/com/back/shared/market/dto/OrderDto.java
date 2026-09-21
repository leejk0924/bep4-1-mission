package com.back.shared.market.dto;

import com.back.boundedContext.market.domain.Order;

import java.time.LocalDateTime;

public record OrderDto(
        int id,
        LocalDateTime createDate,
        LocalDateTime modifyDate,
        int customerId,
        String customerName,
        long price,
        long salePrice,
        LocalDateTime requestPaymentDate,
        LocalDateTime paymentDate
) {
    public OrderDto(Order order) {
        this(
                order.getId(),
                order.getCreateDate(),
                order.getModifyDate(),
                order.getBuyer().getId(),
                order.getBuyer().getNickname(),
                order.getPrice(),
                order.getSalePrice(),
                order.getRequestPaymentDate(),
                order.getPaymentDate()
        );
    }
}
