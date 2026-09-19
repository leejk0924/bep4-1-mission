package com.back.boundedContext.member.domain;

import com.back.global.GlobalConfig;
import com.back.global.eventPublisher.EventPublisher;
import com.back.shared.member.event.MemberModifiedEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class MemberTest {

    @Mock
    private EventPublisher eventPublisher;

    private final Member sut = new Member("user1", "1234", "유저1");

    @BeforeEach
    void wireGlobalEventPublisher() {
        new GlobalConfig().setEventPublisher(eventPublisher);
    }

    @AfterEach
    void resetGlobalEventPublisher() {
        new GlobalConfig().setEventPublisher(null);
    }

    @Test
    @DisplayName("increaseActivityScore(0)은 점수를 변경하지 않고 이벤트도 발행하지 않는다")
    void increaseActivityScore_zero_doesNotChangeOrPublish() {
        int result = sut.increaseActivityScore(0);

        assertThat(result).isEqualTo(0);
        assertThat(sut.getActivityScore()).isEqualTo(0);
        then(eventPublisher).should(never()).publish(any());
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("increaseActivityScoreCases")
    @DisplayName("increaseActivityScore(amount)는 점수에 반영하고 변경 이벤트를 발행한다")
    void increaseActivityScore_nonZero_changesScoreAndPublishesEvent(
            String description, int amount, int expectedScore
    ) {
        int result = sut.increaseActivityScore(amount);

        assertThat(result).isEqualTo(expectedScore);
        assertThat(sut.getActivityScore()).isEqualTo(expectedScore);

        ArgumentCaptor<MemberModifiedEvent> captor = ArgumentCaptor.forClass(MemberModifiedEvent.class);
        then(eventPublisher).should().publish(captor.capture());
        assertThat(captor.getValue().member().activityScore()).isEqualTo(expectedScore);
    }

    private static Stream<Arguments> increaseActivityScoreCases() {
        return Stream.of(
                of("양수만큼 증가한다", 3, 3),
                of("음수만큼 감소한다", -1, -1)
        );
    }
}
