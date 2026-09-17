package com.back.boundedContext.post.domain;

import com.back.global.jpa.entity.BaseIdAndTimeManual;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "POST_MEMBER")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostMember extends BaseIdAndTimeManual {
    @Column(unique = true)
    private String username;
    private String password;
    private String nickname;
    private int activityScore;
}
