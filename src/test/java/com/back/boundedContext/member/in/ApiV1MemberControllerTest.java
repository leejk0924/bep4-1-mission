package com.back.boundedContext.member.in;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = MOCK)
class ApiV1MemberControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("GET [/api/v1/member/members/randomSecureTip] 은 비밀번호 변경 주기 보안 팁을 반환한다")
    void getRandomSecureTip_returnsPasswordChangeTip() throws Exception {
        mvc.perform(get("/api/v1/member/members/randomSecureTip"))
                .andExpect(status().isOk())
                .andExpect(content().string("비밀번호의 유효기간은 90일 입니다."));
    }
}
