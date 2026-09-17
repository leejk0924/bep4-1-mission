package com.back.shared.post.evnet;

import com.back.shared.post.dto.PostCommentDto;

public record PostCommentCreatedEvent(PostCommentDto postComment) {
}
