package com.health.system.common.exception;

import lombok.Getter;

/**
 * 业务异常
 * 用于 Service 层抛出可预知的业务逻辑错误
 * 由 GlobalExceptionHandler 统一捕获处理
 *
 * @author health-system
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
    }
}
