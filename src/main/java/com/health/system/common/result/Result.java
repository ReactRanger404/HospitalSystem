package com.health.system.common.result;

import lombok.Data;

/**
 * 统一 API 响应结果封装
 * 所有 Controller 接口统一使用此格式返回数据
 *
 * @param <T> 数据类型
 * @author health-system
 */
@Data
public class Result<T> {

    /** 状态码 */
    private Integer code;
    /** 消息 */
    private String message;
    /** 数据 */
    private T data;
    /** 时间戳 */
    private Long timestamp;

    private Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = 200;
        result.message = "操作成功";
        result.data = data;
        return result;
    }

    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.code = 200;
        result.message = message;
        result.data = data;
        return result;
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        return result;
    }

    public static <T> Result<T> error(String message) {
        return error(500, message);
    }

    public static <T> Result<T> unauthorized(String message) {
        return error(401, message);
    }

    public static <T> Result<T> forbidden(String message) {
        return error(403, message);
    }

    public static <T> Result<T> notFound(String message) {
        return error(404, message);
    }

    public static <T> Result<T> badRequest(String message) {
        return error(400, message);
    }
}
