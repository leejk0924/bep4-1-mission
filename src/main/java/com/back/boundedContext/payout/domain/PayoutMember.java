package com.back.boundedContext.payout.domain;

import com.back.shared.member.domain.ReplicaMember;
import com.back.shared.payout.dto.PayoutMemberDto;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "PAYOUT_MEMBER")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PayoutMember extends ReplicaMember {

    public PayoutMember(
            int id,
            LocalDateTime createDate,
            LocalDateTime modifyDate,
            String username,
            String password,
            String nickname,
            int activityScore
    ) {
        super(id, createDate, modifyDate, username, password, nickname, activityScore);
    }

    public PayoutMemberDto toDto() {
        return new PayoutMemberDto(
                getId(),
                getCreateDate(),
                getModifyDate(),
                getUsername(),
                getNickname(),
                getActivityScore()
        );
    }
}
