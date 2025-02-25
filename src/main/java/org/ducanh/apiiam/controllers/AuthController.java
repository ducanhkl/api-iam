package org.ducanh.apiiam.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@Tag(name = "Auth controller")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("register")
    @Operation(summary = "Register user account")
    public ResponseEntity<UserRegisterResponseDto> register(
            @RequestBody @Valid UserRegisterRequestDto userRegisterRequestDto,
            @RequestHeader(value = "namespace-id") String namespaceId) {
        return ResponseEntity.ok()
                .body(authService.register(userRegisterRequestDto, namespaceId));
    }

    @PostMapping("login")
    @Operation(summary = "User login and get the token")
    public ResponseEntity<UserLoginResponseDto> login(
            @RequestBody @Valid UserLoginRequestDto userLoginRequestDto,
            @RequestHeader("ip-address") String ipAddress,
            @RequestHeader("user-agent") String userAgent) {
        return ResponseEntity.ok().body(authService.login(userLoginRequestDto, ipAddress, userAgent));
    }

    @PutMapping("verify/{username}")
    @Operation(summary = "Sent  the otp for user to active user")
    public ResponseEntity<?> verify(@PathVariable("username") String username,
                                    @RequestHeader("namespace-id") String namespaceId) {
        authService.verify(username, namespaceId);
        return ResponseEntity.ok().build();
    }


    @PutMapping("/token/refresh")
    @Operation(summary = "Renew access token to generate new session and disable old session")
    public ResponseEntity<TokenRefreshResponse> renewAccessToken(
            @RequestHeader("refresh-token") String refreshToken,
            @RequestHeader("user-agent") String userAgent,
            @RequestHeader("ip-address") String ipAddress
    ) {
        return ResponseEntity.ok(authService.renewAccessToken(refreshToken, userAgent, ipAddress));
    }

    @DeleteMapping("/logout")
    @Operation(summary = "Logout from user session")
    public void logout(@RequestHeader("refresh-token") String refreshToken) {
        authService.logout(refreshToken);
    }

    @PutMapping("complete-verify/{username}")
    @Operation(summary = "Input otp token to very account")
    public ResponseEntity<?> completeVerify(
            @PathVariable("username") String username,
            @RequestHeader("namespace-id") String namespaceId,
            @RequestHeader("code") String code
    ) {
        authService.completeVerify(username, namespaceId, code);
        return ResponseEntity.ok().build();
    }
}
