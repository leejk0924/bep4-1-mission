package com.back.shared.cash.dto;

import com.back.boundedContext.cash.domain.CashMember;

import java.time.LocalDateTime;

public record CashMemberDto(
        int id,
        LocalDateTime createDate,
        LocalDateTime modifyDate,
        String username,
        String nickname,
        int activityScore
) {
    public CashMemberDto(CashMember member) {
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
