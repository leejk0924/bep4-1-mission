package com.back.boundedContext.member.domain;

import com.back.shared.member.domain.SourceMember;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(name = "MEMBER_MEMBER")
@NoArgsConstructor(access = PROTECTED)
public class Member extends SourceMember {
    public Member(String username, String password, String nickname) {
        super(username, password,  nickname);
    }

    public int increaseActivityScore(int amount) {
        setActivityScore(getActivityScore() + amount);
        return getActivityScore();
    }
}