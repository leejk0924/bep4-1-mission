package com.back.shared.post;

import com.back.shared.dto.PostDto;

public record PostCreatedEvent(PostDto post) {
}
