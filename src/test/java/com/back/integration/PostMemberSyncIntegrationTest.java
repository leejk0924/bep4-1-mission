package com.back.integration;

import com.back.boundedContext.member.app.MemberFacade;
import com.back.boundedContext.member.domain.Member;
import com.back.boundedContext.post.app.PostFacade;
import com.back.boundedContext.post.domain.PostMember;
import com.back.boundedContext.post.out.PostMemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("회원 가입/활동점수 변경에 따른 PostMember 복제본 동기화 통합 테스트")
class PostMemberSyncIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MemberFacade memberFacade;

    @Autowired
    private PostFacade postFacade;

    @Autowired
    private PostMemberRepository postMemberRepository;

    @Test
    @DisplayName("회원 가입 시 같은 ID의 PostMember 복제본이 생성되고 필드가 일치하며 비밀번호는 복사되지 않는다")
    void join_createsMatchingReplicaWithoutPassword() {
        String username = uniqueUsername("sync-join");

        Member member = memberFacade.join(username, "1234", "동기화닉네임").data();
        PostMember replica = postFacade.findMemberByUsername(username).orElseThrow();

        assertThat(replica.getId()).isEqualTo(member.getId());
        assertThat(replica.getUsername()).isEqualTo(member.getUsername());
        assertThat(replica.getNickname()).isEqualTo(member.getNickname());
        assertThat(replica.getActivityScore()).isEqualTo(member.getActivityScore());
        assertThat(replica.getCreateDate()).isEqualTo(member.getCreateDate());
        assertThat(replica.getModifyDate()).isEqualTo(member.getModifyDate());
        assertThat(replica.getPassword()).isEmpty();
    }

    @Test
    @DisplayName("활동점수가 바뀌면 기존 PostMember 행이 갱신되고 새 행이 추가되지 않는다")
    void scoreChange_updatesExistingReplicaWithoutDuplicating() {
        String username = uniqueUsername("sync-score");
        Member member = memberFacade.join(username, "1234", "동기화닉네임2").data();
        PostMember authorBeforeScoreChange = postFacade.findMemberByUsername(username).orElseThrow();
        long countBefore = postMemberRepository.count();

        postFacade.write(authorBeforeScoreChange, "제목", "내용");

        Member updatedMember = memberFacade.findById(member.getId()).orElseThrow();
        PostMember updatedReplica = postFacade.findMemberByUsername(username).orElseThrow();
        long countAfter = postMemberRepository.count();

        assertThat(updatedMember.getActivityScore()).isEqualTo(3);
        assertThat(updatedReplica.getId()).isEqualTo(member.getId());
        assertThat(updatedReplica.getActivityScore()).isEqualTo(3);
        assertThat(updatedReplica.getUsername()).isEqualTo(updatedMember.getUsername());
        assertThat(updatedReplica.getNickname()).isEqualTo(updatedMember.getNickname());
        assertThat(countAfter).isEqualTo(countBefore);
    }
}
