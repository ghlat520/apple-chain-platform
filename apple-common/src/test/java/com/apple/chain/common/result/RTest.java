package com.apple.chain.common.result;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for R (API response wrapper).
 * Pure POJO tests — no Spring context required.
 */
@DisplayName("R 响应包装器单元测试")
class RTest {

    // ─── R.ok() ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("ok() - 无数据时返回200成功码和null数据")
    void ok_noArgs_returnsSuccessCodeAndNullData() {
        R<Void> result = R.ok();

        assertThat(result.getCode()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(result.getMessage()).isEqualTo(ResultCode.SUCCESS.getMessage());
        assertThat(result.getData()).isNull();
        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("ok(data) - 包装数据并返回200成功码")
    void ok_withData_wrapsDataAndReturnsSuccessCode() {
        String payload = "测试数据";
        R<String> result = R.ok(payload);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo("测试数据");
        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("ok(data) - 数据为集合类型")
    void ok_withListData_wrapsListCorrectly() {
        List<String> list = List.of("apple", "pear", "orange");
        R<List<String>> result = R.ok(list);

        assertThat(result.getData()).hasSize(3);
        assertThat(result.getData()).contains("apple");
    }

    @Test
    @DisplayName("ok(message, data) - 自定义消息和数据")
    void ok_withMessageAndData_returnsCustomMessage() {
        R<Integer> result = R.ok("创建成功", 42);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("创建成功");
        assertThat(result.getData()).isEqualTo(42);
        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("ok(data) - null数据不影响成功状态")
    void ok_withNullData_stillSuccess() {
        R<String> result = R.ok((String) null);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isNull();
    }

    // ─── R.fail() ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("fail(message) - 返回400失败码和自定义消息")
    void fail_withMessage_returnsDefaultFailCodeAndMessage() {
        R<Void> result = R.fail("操作失败，请重试");

        assertThat(result.getCode()).isEqualTo(ResultCode.FAIL.getCode());
        assertThat(result.getMessage()).isEqualTo("操作失败，请重试");
        assertThat(result.getData()).isNull();
        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    @DisplayName("fail(code, message) - 返回指定错误码和消息")
    void fail_withCodeAndMessage_returnsCustomCodeAndMessage() {
        R<Void> result = R.fail(404, "资源不存在");

        assertThat(result.getCode()).isEqualTo(404);
        assertThat(result.getMessage()).isEqualTo("资源不存在");
        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    @DisplayName("fail(ResultCode) - 从枚举获取code和message")
    void fail_withResultCode_returnsEnumCodeAndMessage() {
        R<Void> result = R.fail(ResultCode.UNAUTHORIZED);

        assertThat(result.getCode()).isEqualTo(401);
        assertThat(result.getMessage()).isEqualTo(ResultCode.UNAUTHORIZED.getMessage());
        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    @DisplayName("fail(ResultCode.NOT_FOUND) - 404码")
    void fail_notFound_returns404() {
        R<Void> result = R.fail(ResultCode.NOT_FOUND);

        assertThat(result.getCode()).isEqualTo(404);
    }

    @Test
    @DisplayName("fail(ResultCode.INTERNAL_ERROR) - 500码")
    void fail_internalError_returns500() {
        R<Void> result = R.fail(ResultCode.INTERNAL_ERROR);

        assertThat(result.getCode()).isEqualTo(500);
    }

    // ─── isSuccess ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("isSuccess - 200码返回true")
    void isSuccess_successCode_returnsTrue() {
        R<String> result = R.ok("data");

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("isSuccess - 非200码返回false")
    void isSuccess_nonSuccessCode_returnsFalse() {
        R<Void> result = R.fail("error");

        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    @DisplayName("isSuccess - 自定义401码返回false")
    void isSuccess_401Code_returnsFalse() {
        R<Void> result = R.fail(401, "未授权");

        assertThat(result.isSuccess()).isFalse();
    }

    // ─── ResultCode enum ──────────────────────────────────────────────────────

    @Test
    @DisplayName("ResultCode - 所有枚举值均有正确的code和message")
    void resultCode_allEnumsHaveCodeAndMessage() {
        assertThat(ResultCode.SUCCESS.getCode()).isEqualTo(200);
        assertThat(ResultCode.FAIL.getCode()).isEqualTo(400);
        assertThat(ResultCode.UNAUTHORIZED.getCode()).isEqualTo(401);
        assertThat(ResultCode.FORBIDDEN.getCode()).isEqualTo(403);
        assertThat(ResultCode.NOT_FOUND.getCode()).isEqualTo(404);
        assertThat(ResultCode.PARAM_ERROR.getCode()).isEqualTo(422);
        assertThat(ResultCode.INTERNAL_ERROR.getCode()).isEqualTo(500);

        for (ResultCode code : ResultCode.values()) {
            assertThat(code.getMessage()).isNotBlank();
        }
    }
}
