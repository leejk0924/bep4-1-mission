package com.back.shared.market.dto;

import com.back.boundedContext.market.domain.MarketMember;

import java.time.LocalDateTime;

public record MarketMemberDto(
        int id,
        LocalDateTime createDate,
        LocalDateTime modifyDate,
        String username,
        String nickname,
        int activityScore
) {
    public MarketMemberDto(MarketMember member) {
        this(
                member.getId(),
                member.getCreateDate(),
                member.getModifyDate(),
                member.getUsername(),
                member.getNickname(),
                member.getActivityScore()
        );
    }
}
