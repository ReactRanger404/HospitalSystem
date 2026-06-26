package com.health.system.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 登录响应 DTO
 *
 * @author health-system
 */
@Data
@Builder
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginResponse {

    @Schema(description = "JWT 访问令牌")
    private String accessToken;

    @Schema(description = "令牌类型")
    private String tokenType = "Bearer";

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "角色")
    private String role;

    @Schema(description = "科室ID")
    private Long departmentId;
}
