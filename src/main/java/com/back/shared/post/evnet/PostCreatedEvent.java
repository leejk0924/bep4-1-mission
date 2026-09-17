package com.back.shared.post.evnet;

import com.back.shared.post.dto.PostDto;

public record PostCreatedEvent(PostDto post) {
}
