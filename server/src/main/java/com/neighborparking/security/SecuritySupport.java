package com.neighborparking.security;

import com.neighborparking.common.error.BusinessException;
import com.neighborparking.common.error.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecuritySupport {

    private SecuritySupport() {
    }

    public static CurrentUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CurrentUser)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "请先登录");
        }
        return (CurrentUser) authentication.getPrincipal();
    }
}
