package com.apple.chain.common.util;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for JwtUtil.
 * Uses ReflectionTestUtils to inject @Value fields without Spring context.
 */
@DisplayName("JwtUtil 单元测试")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    // Must be at least 256 bits (32 chars) for HMAC-SHA256
    private static final String TEST_SECRET =
            "apple-chain-platform-test-jwt-secret-key-minimum-256bits";
    private static final long TEST_EXPIRATION_MS = 3_600_000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", TEST_EXPIRATION_MS);
    }

    // ─── generateToken ────────────────────────────────────────────────────────

    @Test
    @DisplayName("generateToken - 返回非空token字符串")
    void generateToken_returnsNonNullToken() {
        String token = jwtUtil.generateToken(1L, "admin", "ADMIN");

        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("generateToken - 生成的token为3段JWT格式（header.payload.signature）")
    void generateToken_isJwtFormat() {
        String token = jwtUtil.generateToken(100L, "farmer01", "FARMER");

        String[] parts = token.split("\\.");
        assertThat(parts).hasSize(3);
    }

    @Test
    @DisplayName("generateToken - 相同参数生成的token包含相同的claims")
    void generateToken_sameParams_claimsMatch() {
        String token = jwtUtil.generateToken(42L, "testUser", "OPERATOR");

        assertThat(jwtUtil.getUserId(token)).isEqualTo(42L);
        assertThat(jwtUtil.getUsername(token)).isEqualTo("testUser");
        assertThat(jwtUtil.getRoleCode(token)).isEqualTo("OPERATOR");
    }

    // ─── parseToken ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("parseToken - 有效token返回Claims")
    void parseToken_validToken_returnsClaims() {
        String token = jwtUtil.generateToken(1L, "admin", "ADMIN");

        Claims claims = jwtUtil.parseToken(token);

        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo("admin");
    }

    @Test
    @DisplayName("parseToken - 无效token（随机字符串）返回null")
    void parseToken_randomString_returnsNull() {
        Claims claims = jwtUtil.parseToken("this.is.not.a.valid.jwt");

        assertThat(claims).isNull();
    }

    @Test
    @DisplayName("parseToken - 空字符串返回null")
    void parseToken_emptyString_returnsNull() {
        Claims claims = jwtUtil.parseToken("");

        assertThat(claims).isNull();
    }

    @Test
    @DisplayName("parseToken - null输入返回null（不抛异常）")
    void parseToken_null_returnsNull() {
        assertThatCode(() -> {
            Claims claims = jwtUtil.parseToken(null);
            assertThat(claims).isNull();
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("parseToken - 使用不同密钥签名的token验证失败返回null")
    void parseToken_wrongSecret_returnsNull() {
        // Generate token with different secret
        JwtUtil otherUtil = new JwtUtil();
        ReflectionTestUtils.setField(otherUtil, "secret",
                "completely-different-secret-key-for-testing-purposes-256bits");
        ReflectionTestUtils.setField(otherUtil, "expirationMs", TEST_EXPIRATION_MS);

        String tokenFromOtherKey = otherUtil.generateToken(1L, "admin", "ADMIN");

        // Verify with original util using different key — must fail
        Claims claims = jwtUtil.parseToken(tokenFromOtherKey);
        assertThat(claims).isNull();
    }

    // ─── getUserId ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getUserId - 从有效token提取userId")
    void getUserId_validToken_returnsUserId() {
        String token = jwtUtil.generateToken(12345L, "user01", "FARMER");

        Long userId = jwtUtil.getUserId(token);

        assertThat(userId).isEqualTo(12345L);
    }

    @Test
    @DisplayName("getUserId - 无效token返回null")
    void getUserId_invalidToken_returnsNull() {
        Long userId = jwtUtil.getUserId("invalid.token.here");

        assertThat(userId).isNull();
    }

    // ─── getUsername ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getUsername - 从有效token提取username")
    void getUsername_validToken_returnsUsername() {
        String token = jwtUtil.generateToken(1L, "testFarmer", "FARMER");

        String username = jwtUtil.getUsername(token);

        assertThat(username).isEqualTo("testFarmer");
    }

    @Test
    @DisplayName("getUsername - 无效token返回null")
    void getUsername_invalidToken_returnsNull() {
        String username = jwtUtil.getUsername("bad.token");

        assertThat(username).isNull();
    }

    // ─── getRoleCode ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getRoleCode - 从有效token提取roleCode")
    void getRoleCode_validToken_returnsRoleCode() {
        String token = jwtUtil.generateToken(1L, "admin", "ADMIN");

        String roleCode = jwtUtil.getRoleCode(token);

        assertThat(roleCode).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("getRoleCode - 不同角色均可正确提取")
    void getRoleCode_differentRoles_allExtractCorrectly() {
        String[] roles = {"ADMIN", "FARMER", "TRADER", "OPERATOR"};

        for (String role : roles) {
            String token = jwtUtil.generateToken(1L, "user", role);
            assertThat(jwtUtil.getRoleCode(token)).isEqualTo(role);
        }
    }

    // ─── isTokenValid ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("isTokenValid - 有效token返回true")
    void isTokenValid_validToken_returnsTrue() {
        String token = jwtUtil.generateToken(1L, "admin", "ADMIN");

        assertThat(jwtUtil.isTokenValid(token)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid - 无效token返回false")
    void isTokenValid_invalidToken_returnsFalse() {
        assertThat(jwtUtil.isTokenValid("not.a.valid.jwt")).isFalse();
    }

    @Test
    @DisplayName("isTokenValid - 过期token返回false")
    void isTokenValid_expiredToken_returnsFalse() {
        JwtUtil expiredUtil = new JwtUtil();
        ReflectionTestUtils.setField(expiredUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(expiredUtil, "expirationMs", -1000L); // already expired

        String expiredToken = expiredUtil.generateToken(1L, "admin", "ADMIN");

        assertThat(jwtUtil.isTokenValid(expiredToken)).isFalse();
    }
}
