package com.back.shared.member.domain;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PROTECTED;

@Getter
@MappedSuperclass
@NoArgsConstructor(access = PROTECTED)
public abstract class ReplicaMember extends BaseMember {
    @Id
    private int id;
    private LocalDateTime createDate;
    private LocalDateTime modifyDate;

    public ReplicaMember(
            int id,
            LocalDateTime createDate,
            LocalDateTime modifyDate,
            String username,
            String password,
            String nickname
    ) {
        super(username, password, nickname);
        this.id = id;
        this.createDate = createDate;
        this.modifyDate = modifyDate;
    }
}
