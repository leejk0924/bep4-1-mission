package com.back.boundedContext.post.app;

import com.back.boundedContext.post.domain.Post;
import com.back.boundedContext.post.domain.PostMember;
import com.back.boundedContext.post.out.PostRepository;
import com.back.global.eventPublisher.EventPublisher;
import com.back.global.rsData.RsData;
import com.back.shared.member.out.MemberApiClient;
import com.back.shared.post.evnet.PostCreatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class PostWriteUseCaseTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private MemberApiClient memberApiClient;

    @InjectMocks
    private PostWriteUseCase sut;

    @Test
    @DisplayName("write()는 글을 저장하고 생성 이벤트를 발행하며 보안 팁이 포함된 결과를 반환한다")
    void write_savesPostPublishesEventAndReturnsResultWithSecureTip() {
        PostMember author = new PostMember(
                1, LocalDateTime.now(), LocalDateTime.now(), "user1", "", "유저1", 0
        );
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(memberApiClient.getRandomSecureTip()).willReturn("비밀번호의 유효기간은 90일 입니다.");

        RsData<Post> result = sut.write(author, "제목1", "내용1");

        assertThat(result.resultCode()).isEqualTo("201-1");
        assertThat(result.msg()).isEqualTo("0번 글이 생성되었습니다. 보안 팁 : 비밀번호의 유효기간은 90일 입니다.");
        assertThat(result.data().getTitle()).isEqualTo("제목1");
        assertThat(result.data().getContent()).isEqualTo("내용1");

        ArgumentCaptor<PostCreatedEvent> captor = ArgumentCaptor.forClass(PostCreatedEvent.class);
        then(eventPublisher).should().publish(captor.capture());
        PostCreatedEvent postCreatedEvent = captor.getValue();

        assertThat(postCreatedEvent.post().title()).isEqualTo("제목1");
        assertThat(postCreatedEvent.post().content()).isEqualTo("내용1");
    }

    @Test
    @DisplayName("write()는 memberApiClient.getRandomSecureTip()이 실패하면 예외를 그대로 전파한다")
    void write_whenSecureTipFails_propagatesException() {
        PostMember author = new PostMember(
                1, LocalDateTime.now(), LocalDateTime.now(), "user1", "", "유저1", 0
        );
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(memberApiClient.getRandomSecureTip()).willThrow(new RestClientException("연결 실패"));

        assertThatThrownBy(() -> sut.write(author, "제목1", "내용1"))
                .isInstanceOf(RestClientException.class);

        then(postRepository).should().save(any(Post.class));
        then(eventPublisher).should().publish(any(PostCreatedEvent.class));
    }
}
