package com.back.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.util.UUID;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@SpringBootTest(webEnvironment = DEFINED_PORT)
abstract class AbstractIntegrationTest {

    @DynamicPropertySource
    static void useFreePort(DynamicPropertyRegistry registry) {
        int port = findFreePort();
        registry.add("server.port", () -> port);
        registry.add("member.api.base-url", () -> "http://localhost:%d/api/v1/member".formatted(port));
    }

    private static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected String uniqueUsername(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
