package com.back.integration;

import com.back.boundedContext.member.app.MemberFacade;
import com.back.boundedContext.member.domain.Member;
import com.back.boundedContext.post.app.PostFacade;
import com.back.boundedContext.post.domain.Post;
import com.back.boundedContext.post.domain.PostMember;
import com.back.global.rsData.RsData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("글/댓글 작성에 따른 활동점수 반영 통합 테스트")
class ActivityScoreIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MemberFacade memberFacade;

    @Autowired
    private PostFacade postFacade;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private PostMember joinAndGetReplica(String usernamePrefix) {
        String username = uniqueUsername(usernamePrefix);
        Member member = memberFacade.join(username, "1234", usernamePrefix).data();

        return postFacade.findMemberByUsername(member.getUsername()).orElseThrow();
    }

    @Test
    @DisplayName("글을 작성하면 작성자의 활동점수가 3점 증가한다")
    void writePost_increasesAuthorScoreByThree() {
        PostMember author = joinAndGetReplica("post-writer");

        postFacade.write(author, "제목", "내용");

        Member updated = memberFacade.findById(author.getId()).orElseThrow();
        assertThat(updated.getActivityScore()).isEqualTo(3);
    }

    @Test
    @DisplayName("댓글을 작성하면 댓글 작성자의 활동점수가 1점 증가한다")
    void writeComment_increasesCommenterScoreByOne() {
        PostMember postAuthor = joinAndGetReplica("comment-post-author");
        PostMember commenter = joinAndGetReplica("commenter");
        Post post = postFacade.write(postAuthor, "제목", "내용").data();

        postFacade.writeComment(post.getId(), commenter, "댓글");

        Member updated = memberFacade.findById(commenter.getId()).orElseThrow();
        assertThat(updated.getActivityScore()).isEqualTo(1);
    }

    @Test
    @DisplayName("글 작성이 롤백되면 글도 저장되지 않고 작성자의 활동점수도 증가하지 않는다")
    void writePost_rolledBack_doesNotPersistPostOrIncreaseScore() {
        PostMember author = joinAndGetReplica("rollback-writer");
        int[] writtenPostId = new int[1];

        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            RsData<Post> result = postFacade.write(author, "제목", "내용");
            writtenPostId[0] = result.data().getId();
            status.setRollbackOnly();
        });

        assertThat(postFacade.findById(writtenPostId[0])).isEmpty();

        Member updated = memberFacade.findById(author.getId()).orElseThrow();
        assertThat(updated.getActivityScore()).isEqualTo(0);
    }
}
