package com.back.boundedContext.post.domain;

import com.back.global.GlobalConfig;
import com.back.global.eventPublisher.EventPublisher;
import com.back.shared.post.evnet.PostCommentCreatedEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class PostTest {

    @Mock
    private EventPublisher eventPublisher;

    private static final PostMember author = new PostMember(
            1, LocalDateTime.now(), LocalDateTime.now(), "user1", "", "유저1", 0
    );

    private final Post sut = new Post(author, "제목1", "내용1");

    @BeforeEach
    void wireGlobalEventPublisher() {
        new GlobalConfig().setEventPublisher(eventPublisher);
    }

    @AfterEach
    void resetGlobalEventPublisher() {
        new GlobalConfig().setEventPublisher(null);
    }

    @Test
    @DisplayName("hasComments()는 댓글이 없으면 false를 반환한다")
    void hasComments_noComments_returnsFalse() {
        assertThat(sut.hasComments()).isFalse();
    }

    @Test
    @DisplayName("addComment()는 댓글을 추가하고 hasComments()는 true가 된다")
    void addComment_addsCommentAndHasCommentsBecomesTrue() {
        String content = "댓글1";

        PostComment comment = sut.addComment(author, content);

        assertThat(sut.hasComments()).isTrue();
        assertThat(comment.getPost()).isSameAs(sut);
        assertThat(comment.getAuthor()).isSameAs(author);
        assertThat(comment.getContent()).isEqualTo(content);
    }

    @Test
    @DisplayName("addComment()는 PostCommentCreatedEvent를 발행한다")
    void addComment_publishesPostCommentCreatedEvent() {
        String content = "댓글1";

        sut.addComment(author, content);

        ArgumentCaptor<PostCommentCreatedEvent> captor = ArgumentCaptor.forClass(PostCommentCreatedEvent.class);
        then(eventPublisher).should().publish(captor.capture());

        assertThat(captor.getValue().postComment().content()).isEqualTo(content);
    }
}
