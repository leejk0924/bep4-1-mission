package com.back.boundedContext.post.domain;

import com.back.shared.member.domain.ReplicaMember;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(name = "POST_MEMBER")
@NoArgsConstructor(access = PROTECTED)
public class PostMember extends ReplicaMember {
    public PostMember(String username, String password, String nickname) {
        super(username, password, nickname);
    }
}
