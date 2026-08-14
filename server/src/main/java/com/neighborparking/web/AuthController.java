package com.neighborparking.web;

import com.neighborparking.common.error.BusinessException;
import com.neighborparking.common.error.ErrorCode;
import com.neighborparking.domain.AppUser;
import com.neighborparking.domain.enums.RiskStatus;
import com.neighborparking.domain.enums.UserRole;
import com.neighborparking.repository.AppUserRepository;
import com.neighborparking.security.AuthProperties;
import com.neighborparking.security.JwtService;
import com.neighborparking.security.SecuritySupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthProperties authProperties;
    private final AppUserRepository userRepository;
    private final JwtService jwtService;

    public AuthController(AuthProperties authProperties, AppUserRepository userRepository, JwtService jwtService) {
        this.authProperties = authProperties;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @PostMapping("/dev-login")
    @Operation(summary = "使用本地演示身份登录", security = {})
    @SecurityRequirements
    public LoginResponse devLogin(@Valid @RequestBody DevLoginRequest request) {
        if (!authProperties.isDevEnabled()) {
            throw new BusinessException(ErrorCode.DEV_LOGIN_DISABLED, HttpStatus.NOT_FOUND, "开发登录未启用");
        }
        AppUser user = userRepository.findById(request.getUserId()).orElseThrow(() ->
                new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND, "演示用户不存在"));
        if (user.getRiskStatus() == RiskStatus.BLOCKED) {
            throw new BusinessException(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "该身份已被停用");
        }
        return new LoginResponse(jwtService.createToken(user), UserView.from(user));
    }

    @GetMapping("/me")
    public UserView me() {
        AppUser user = userRepository.findById(SecuritySupport.currentUser().getUserId()).orElseThrow(() ->
                new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND, "用户不存在"));
        return UserView.from(user);
    }

    @Data
    @NoArgsConstructor
    public static class DevLoginRequest {
        @NotNull(message = "用户 ID 不能为空")
        private Long userId;
    }

    @Data
    @AllArgsConstructor
    public static class LoginResponse {
        private String accessToken;
        private UserView user;
    }

    @Data
    @AllArgsConstructor
    public static class UserView {
        private Long id;
        private String nickname;
        private String phoneMasked;
        private String avatarUrl;
        private Set<UserRole> roles;

        public static UserView from(AppUser user) {
            return new UserView(user.getId(), user.getNickname(), user.getPhoneMasked(), user.getAvatarUrl(),
                    user.getRoles());
        }
    }
}
