package com.back.boundedContext.member.app;

import com.back.boundedContext.member.domain.Member;
import com.back.boundedContext.member.out.MemberRepository;
import com.back.global.rsData.RsData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class MemberFacadeTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberJoinUseCase memberJoinUseCase;

    @InjectMocks
    private MemberFacade sut;

    @Test
    @DisplayName("count()는 memberRepository.count()의 결과를 그대로 반환한다")
    void count_delegatesToRepository() {
        given(memberRepository.count()).willReturn(6L);

        long count = sut.count();

        assertThat(count).isEqualTo(6L);
    }

    @Test
    @DisplayName("join()은 memberJoinUseCase.join()에 위임한다")
    void join_delegatesToJoinUseCase() {
        RsData<Member> expected = new RsData<>("201-1", "1번 회원이 생성되었습니다.");
        given(memberJoinUseCase.join("user1", "1234", "유저1")).willReturn(expected);

        RsData<Member> result = sut.join("user1", "1234", "유저1");

        assertThat(result).isSameAs(expected);
        then(memberJoinUseCase).should().join("user1", "1234", "유저1");
    }

    @Test
    @DisplayName("findByUsername()은 memberRepository.findByUsername()의 결과를 그대로 반환한다")
    void findByUsername_delegatesToRepository() {
        Member member = new Member("user1", "1234", "유저1");
        given(memberRepository.findByUsername("user1")).willReturn(Optional.of(member));

        Optional<Member> result = sut.findByUsername("user1");

        assertThat(result).containsSame(member);
    }

    @Test
    @DisplayName("findById()는 memberRepository.findById()의 결과를 그대로 반환한다")
    void findById_delegatesToRepository() {
        Member member = new Member("user1", "1234", "유저1");
        given(memberRepository.findById(1)).willReturn(Optional.of(member));

        Optional<Member> result = sut.findById(1);

        assertThat(result).containsSame(member);
    }

    @Test
    @DisplayName("getRandomSecureTip()은 90일 비밀번호 변경 주기 문구를 반환한다")
    void getRandomSecureTip_returnsPasswordChangeTip() {
        String tip = sut.getRandomSecureTip();

        assertThat(tip).isEqualTo("비밀번호의 유효기간은 90일 입니다.");
    }
}
