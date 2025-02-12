package org.ducanh.apiiam.controllers;

import lombok.RequiredArgsConstructor;
import org.ducanh.apiiam.dto.requests.UserLoginRequestDto;
import org.ducanh.apiiam.dto.requests.UserRegisterRequestDto;
import org.ducanh.apiiam.dto.responses.TokenRefreshResponse;
import org.ducanh.apiiam.dto.responses.UserLoginResponseDto;
import org.ducanh.apiiam.dto.responses.UserRegisterResponseDto;
import org.ducanh.apiiam.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("register")
    public ResponseEntity<UserRegisterResponseDto> register(
            @RequestBody UserRegisterRequestDto userRegisterRequestDto,
            @RequestHeader(value = "namespace-id", required = true) Long namespaceId) {
        return ResponseEntity.ok()
                .body(authService.register(userRegisterRequestDto, namespaceId));
    }

    @PostMapping("login")
    public ResponseEntity<UserLoginResponseDto> login(
            @RequestBody UserLoginRequestDto userLoginRequestDto,
            @RequestHeader("ip-address") String ipAddress,
            @RequestHeader("user-agent") String userAgent) {
        return ResponseEntity.ok().body(authService.login(userLoginRequestDto, ipAddress, userAgent));
    }

    @PutMapping("verify/{username}")
    public ResponseEntity<?> verify(@PathVariable("username") String username,
                                    @RequestHeader("namespace-id") Long namespaceId) {
        authService.verify(username, namespaceId);
        return ResponseEntity.ok().build();
    }


    @PutMapping("/token/refresh")
    public ResponseEntity<TokenRefreshResponse> renewAccessToken(
            @RequestHeader("refresh-token") String refreshToken,
            @RequestHeader("user-agent") String userAgent,
            @RequestHeader("ip-address") String ipAddress
    ) {
        return ResponseEntity.ok(authService.renewAccessToken(refreshToken, userAgent, ipAddress));
    }

    @PutMapping("complete-verify/{username}")
    public ResponseEntity<?> completeVerify(
            @PathVariable("username") String username,
            @RequestHeader("namespace-id") Long namespaceId,
            @RequestHeader("code") String code
    ) {
        authService.completeVerify(username, namespaceId, code);
        return ResponseEntity.ok().build();
    }
}
