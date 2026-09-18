package com.back.shared.post;

import com.back.shared.dto.PostCommentDto;

public record PostCommentCreatedEvent(PostCommentDto postComment) {
}
