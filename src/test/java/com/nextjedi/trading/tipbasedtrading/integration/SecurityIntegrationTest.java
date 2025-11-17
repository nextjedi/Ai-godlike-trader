package com.nextjedi.trading.tipbasedtrading.integration;

import com.nextjedi.trading.tipbasedtrading.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Security and JWT authentication
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@DisplayName("Security Integration Tests")
class SecurityIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("trading_db_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("Should deny access to protected endpoint without token")
    void shouldDenyAccessToProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/portfolio/user/testuser"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should allow access to protected endpoint with valid token")
    void shouldAllowAccessToProtectedEndpointWithValidToken() throws Exception {
        // Given
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_TRADER"))
        );
        String token = jwtTokenProvider.generateToken(authentication);

        // When/Then
        mockMvc.perform(get("/api/v1/portfolio/user/testuser")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should deny access with invalid token")
    void shouldDenyAccessWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/portfolio/user/testuser")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should deny access with malformed Authorization header")
    void shouldDenyAccessWithMalformedAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/v1/portfolio/user/testuser")
                        .header("Authorization", "InvalidFormat token"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should deny access to admin endpoint for TRADER role")
    void shouldDenyAccessToAdminEndpointForTraderRole() throws Exception {
        // Given
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_TRADER"))
        );
        String token = jwtTokenProvider.generateToken(authentication);

        // When/Then
        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should allow access to admin endpoint for ADMIN role")
    void shouldAllowAccessToAdminEndpointForAdminRole() throws Exception {
        // Given
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "admin",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        String token = jwtTokenProvider.generateToken(authentication);

        // When/Then - Will return 404 since admin endpoints don't exist, but not 403 (forbidden)
        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());  // Not forbidden, just doesn't exist
    }
}
