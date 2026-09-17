package com.back.global.jpa.entity;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
public abstract class BaseIdAndTimeManual extends BaseEntity {
    @Id
    private int id;
    private LocalDateTime createDate;
    private LocalDateTime modifyDate;
}
