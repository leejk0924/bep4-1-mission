package com.back.shared.post.dto;

import com.back.shared.modelType.HashModelTypeCode;

import java.time.LocalDateTime;

public record PostDto (
        int id,
        LocalDateTime createDate,
        LocalDateTime modifyDate,
        int authorId,
        String authorName,
        String title,
        String content
) implements HashModelTypeCode {
    @Override
    public String getModelTypeCode() {
        return "Post";
    }
}
