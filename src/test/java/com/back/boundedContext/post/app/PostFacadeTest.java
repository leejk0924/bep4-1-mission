package com.back.boundedContext.post.app;

import com.back.boundedContext.post.domain.Post;
import com.back.boundedContext.post.domain.PostComment;
import com.back.boundedContext.post.domain.PostMember;
import com.back.boundedContext.post.out.PostMemberRepository;
import com.back.boundedContext.post.out.PostRepository;
import com.back.global.exception.DomainException;
import com.back.global.rsData.RsData;
import com.back.shared.member.dto.MemberDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class PostFacadeTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostMemberRepository postMemberRepository;

    @Mock
    private PostWriteUseCase postWriteUseCase;

    @InjectMocks
    private PostFacade sut;

    @Test
    @DisplayName("count()는 postRepository.count()의 결과를 그대로 반환한다")
    void count_delegatesToRepository() {
        given(postRepository.count()).willReturn(6L);

        long count = sut.count();

        assertThat(count).isEqualTo(6L);
    }

    @Test
    @DisplayName("write()는 postWriteUseCase.write()에 위임한다")
    void write_delegatesToWriteUseCase() {
        PostMember author = new PostMember(
                1,
                LocalDateTime.now(),
                LocalDateTime.now(),
                "user1",
                "",
                "유저1",
                0
        );
        var expected = new RsData<Post>("201-1", "1번 글이 생성되었습니다.");
        given(postWriteUseCase.write(author, "제목1", "내용1"))
                .willReturn(expected);

        RsData<Post> result = sut.write(author, "제목1", "내용1");

        then(postWriteUseCase).should().write(author, "제목1", "내용1");
        assertThat(result).isSameAs(expected);
    }

    @Test
    @DisplayName("findById()는 postRepository.findById()의 결과를 그대로 반환한다")
    void findById_delegatesToRepository() {
        var author = new PostMember(
                1,
                LocalDateTime.now(),
                LocalDateTime.now(),
                "user1",
                "",
                "유저1",
                0
        );
        var post = new Post(author, "제목1", "내용1");
        given(postRepository.findById(1)).willReturn(Optional.of(post));

        Optional<Post> result = sut.findById(1);

        assertThat(result).containsSame(post);
    }

    @Test
    @DisplayName("findById()는 글이 없으면 빈 Optional을 반환한다")
    void findById_whenNotFound_returnsEmpty() {
        given(postRepository.findById(1)).willReturn(Optional.empty());

        Optional<Post> result = sut.findById(1);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("writeComment()는 글을 찾아 댓글을 추가하고 반환한다")
    void writeComment_addsCommentToPost() {
        var author = new PostMember(
                1, LocalDateTime.now(), LocalDateTime.now(), "user1", "", "유저1", 0
        );
        var post = new Post(author, "제목1", "내용1");
        given(postRepository.findById(1)).willReturn(Optional.of(post));

        PostComment result = sut.writeComment(1, author, "댓글1");

        assertThat(result.getContent()).isEqualTo("댓글1");
        assertThat(post.getComments()).containsExactly(result);
    }

    @Test
    @DisplayName("writeComment()는 글이 없으면 404-1 DomainException을 던진다")
    void writeComment_whenPostNotFound_throwsDomainException() {
        var author = new PostMember(
                1, LocalDateTime.now(), LocalDateTime.now(), "user1", "", "유저1", 0
        );
        given(postRepository.findById(1)).willReturn(Optional.empty());

        assertThatThrownBy(() -> sut.writeComment(1, author, "댓글1"))
                .isInstanceOf(DomainException.class)
                .extracting("resultCode")
                .isEqualTo("404-1");
    }

    @Test
    @DisplayName("syncMember()는 MemberDto로 PostMember를 만들어 저장하고 저장된 결과를 반환한다")
    void syncMember_savesAndReturnsPostMember() {
        LocalDateTime now = LocalDateTime.now();
        MemberDto memberDto = new MemberDto(1, now, now, "user1", "유저1", 10);
        PostMember saved = new PostMember(1, now, now, "user1", "", "유저1", 10);
        given(postMemberRepository.save(any())).willReturn(saved);

        PostMember result = sut.syncMember(memberDto);

        assertThat(result).isSameAs(saved);

        ArgumentCaptor<PostMember> captor = ArgumentCaptor.forClass(PostMember.class);
        then(postMemberRepository).should().save(captor.capture());

        assertThat(captor.getValue().getId()).isEqualTo(1);
        assertThat(captor.getValue().getUsername()).isEqualTo("user1");
        assertThat(captor.getValue().getNickname()).isEqualTo("유저1");
        assertThat(captor.getValue().getActivityScore()).isEqualTo(10);
    }

    @Test
    @DisplayName("findMemberByUsername()은 postMemberRepository.findByUsername()의 결과를 그대로 반환한다")
    void findMemberByUsername_delegatesToRepository() {
        PostMember member = new PostMember(
                1, LocalDateTime.now(), LocalDateTime.now(), "user1", "", "유저1", 0
        );
        given(postMemberRepository.findByUsername("user1")).willReturn(Optional.of(member));

        Optional<PostMember> result = sut.findMemberByUsername("user1");

        assertThat(result).containsSame(member);
    }

    @Test
    @DisplayName("findMemberByUsername()은 회원이 없으면 빈 Optional을 반환한다")
    void findMemberByUsername_whenNotFound_returnsEmpty() {
        given(postMemberRepository.findByUsername("user1")).willReturn(Optional.empty());

        Optional<PostMember> result = sut.findMemberByUsername("user1");

        assertThat(result).isEmpty();
    }
}
