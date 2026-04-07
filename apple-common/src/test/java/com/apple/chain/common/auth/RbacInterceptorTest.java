package com.apple.chain.common.auth;

import com.apple.chain.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests {@link RbacInterceptor} end-to-end with a real {@link JwtUtil}.
 * No Spring context — only ReflectionTestUtils to inject @Value fields.
 */
@DisplayName("RbacInterceptor 单元测试")
class RbacInterceptorTest {

    private static final String TEST_SECRET =
            "apple-chain-platform-test-jwt-secret-key-minimum-256bits";

    private JwtUtil jwtUtil;
    private RbacInterceptor interceptor;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 3_600_000L);
        interceptor = new RbacInterceptor(jwtUtil);
    }

    @AfterEach
    void cleanup() {
        PermissionContext.clear();
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private HttpServletRequest reqWithToken(String token) {
        MockHttpServletRequest req = new MockHttpServletRequest();
        if (token != null) {
            req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        req.setRequestURI("/api/test/secure");
        return req;
    }

    private HandlerMethod handlerFor(String methodName) throws NoSuchMethodException {
        Method method = SampleHandler.class.getMethod(methodName);
        return new HandlerMethod(new SampleHandler(), method);
    }

    /** Sample handler exposing methods with various @RequirePerm shapes. */
    static class SampleHandler {
        @RequirePerm("orchard:write")
        public void singlePerm() {
        }

        @RequirePerm(value = {"trade:read", "trade:write"}, logical = RequirePerm.Logical.AND)
        public void andLogic() {
        }

        @RequirePerm(value = {"trade:read", "trade:approve"}, logical = RequirePerm.Logical.OR)
        public void orLogic() {
        }

        public void noAnnotation() {
        }
    }

    // ─── tests ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("有 perms claim 时 PermissionContext 被填充")
    void preHandle_populatesPermissionContext() throws Exception {
        String token = jwtUtil.generateToken(1L, "admin", "ADMIN",
                List.of("ADMIN"), List.of("orchard:write", "orchard:read"));

        boolean ok = interceptor.preHandle(reqWithToken(token), new MockHttpServletResponse(),
                handlerFor("singlePerm"));

        assertThat(ok).isTrue();
        assertThat(PermissionContext.getRoles()).containsExactly("ADMIN");
        assertThat(PermissionContext.getPermissions())
                .containsExactlyInAnyOrder("orchard:write", "orchard:read");
    }

    @Test
    @DisplayName("有 @RequirePerm 但权限不足 → 抛 ForbiddenException")
    void preHandle_missingPermission_throwsForbidden() throws Exception {
        String token = jwtUtil.generateToken(2L, "farmer", "FARMER",
                List.of("FARMER"), List.of("orchard:read"));

        assertThatThrownBy(() -> interceptor.preHandle(
                reqWithToken(token), new MockHttpServletResponse(), handlerFor("singlePerm")))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("orchard:write");
    }

    @Test
    @DisplayName("AND 逻辑 - 缺少任一权限即拒绝")
    void preHandle_andLogic_missingOne_denies() throws Exception {
        String token = jwtUtil.generateToken(3L, "buyer", "BUYER",
                List.of("BUYER"), List.of("trade:read"));

        assertThatThrownBy(() -> interceptor.preHandle(
                reqWithToken(token), new MockHttpServletResponse(), handlerFor("andLogic")))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("AND 逻辑 - 全部权限齐全则放行")
    void preHandle_andLogic_allGranted_passes() throws Exception {
        String token = jwtUtil.generateToken(3L, "buyer", "BUYER",
                List.of("BUYER"), List.of("trade:read", "trade:write"));

        boolean ok = interceptor.preHandle(reqWithToken(token), new MockHttpServletResponse(),
                handlerFor("andLogic"));

        assertThat(ok).isTrue();
    }

    @Test
    @DisplayName("OR 逻辑 - 拥有任一权限即放行")
    void preHandle_orLogic_oneGranted_passes() throws Exception {
        String token = jwtUtil.generateToken(4L, "fin", "FINANCE",
                List.of("FINANCE"), List.of("trade:approve"));

        boolean ok = interceptor.preHandle(reqWithToken(token), new MockHttpServletResponse(),
                handlerFor("orLogic"));

        assertThat(ok).isTrue();
    }

    @Test
    @DisplayName("OR 逻辑 - 一个都没有则拒绝")
    void preHandle_orLogic_noneGranted_denies() throws Exception {
        String token = jwtUtil.generateToken(4L, "fin", "FINANCE",
                List.of("FINANCE"), List.of("finance:read"));

        assertThatThrownBy(() -> interceptor.preHandle(
                reqWithToken(token), new MockHttpServletResponse(), handlerFor("orLogic")))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("无 @RequirePerm 注解的 handler 直接放行")
    void preHandle_noAnnotation_passes() throws Exception {
        String token = jwtUtil.generateToken(5L, "anon", "FARMER",
                List.of("FARMER"), List.of());

        boolean ok = interceptor.preHandle(reqWithToken(token), new MockHttpServletResponse(),
                handlerFor("noAnnotation"));

        assertThat(ok).isTrue();
    }

    @Test
    @DisplayName("非 HandlerMethod (静态资源等) 直接放行")
    void preHandle_nonHandlerMethod_passes() {
        Object resourceHandler = new Object();
        boolean ok = interceptor.preHandle(reqWithToken(null), new MockHttpServletResponse(),
                resourceHandler);
        assertThat(ok).isTrue();
    }

    @Test
    @DisplayName("afterCompletion 清空 PermissionContext")
    void afterCompletion_clearsContext() throws Exception {
        String token = jwtUtil.generateToken(1L, "admin", "ADMIN",
                List.of("ADMIN"), List.of("orchard:write"));
        interceptor.preHandle(reqWithToken(token), new MockHttpServletResponse(),
                handlerFor("singlePerm"));

        assertThat(PermissionContext.getPermissions()).isNotEmpty();

        interceptor.afterCompletion(reqWithToken(token), new MockHttpServletResponse(),
                handlerFor("singlePerm"), null);

        assertThat(PermissionContext.getPermissions()).isEmpty();
    }
}
