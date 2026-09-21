package com.back.boundedContext.market.domain;

import com.back.global.jpa.entity.BaseIdAndTime;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "MARKET_PRODUCT")
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseIdAndTime {
    @ManyToOne(fetch = FetchType.LAZY)
    private MarketMember seller;
    private String sourceTypeCode;
    private int sourceId;
    private String name;
    private String description;
    private long price;
    private long salePrice;
}
