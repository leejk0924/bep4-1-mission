package com.back.boundedContext.member.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

class MemberPolicyTest {
    private final MemberPolicy sut = new MemberPolicy();

    @Test
    @DisplayName("getNeedToChangePasswordPeriod()은 90일을 반환")
    void getNeedToChangePasswordPeriod_returns90Days() {
        Duration passwordPeriod = sut.getNeedToChangePasswordPeriod();

        assertThat(passwordPeriod).isEqualTo(Duration.ofDays(90));
    }

    @Test
    @DisplayName("getNeedToChangePasswordDays()는 90을 반환")
    void getNeedToChangePasswordDays_returns90() {
        int days = sut.getNeedToChangePasswordDays();

        assertThat(days).isEqualTo(90);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("isNeedToChangePasswordCases")
    @DisplayName("isNeedToChangePassword는 마지막 변경일에 따라 변경 필요 여부를 반환한다")
    void isNeedToChangePassword(String description, LocalDateTime lastChangeDate, boolean expected) {
        boolean result = sut.isNeedToChangePassword(lastChangeDate);

        assertThat(result).isEqualTo(expected);
    }

    private static Stream<Arguments> isNeedToChangePasswordCases() {
        LocalDateTime now = LocalDateTime.now();

        return Stream.of(
                of("마지막 변경일이 null이면 true", null, true),
                of("90일보다 오래되면 true", now.minusDays(91), true),
                of("90일 이내면 false", now.minusDays(1), false),
                of("90일 경계를 막 넘기면 true", now.minusDays(90).minusMinutes(1), true),
                of("90일 경계를 막 넘기지 않으면 false", now.minusDays(90).plusMinutes(1), false)
        );
    }
}
