package com.back.shared.post.dto;

import java.time.LocalDateTime;

public record PostDto(
        int id,
        LocalDateTime createDate,
        LocalDateTime modifyDate,
        int authorId,
        String authorName,
        String title,
        String content
) {
}
