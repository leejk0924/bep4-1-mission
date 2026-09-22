package com.back.shared.market.dto;

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
}
