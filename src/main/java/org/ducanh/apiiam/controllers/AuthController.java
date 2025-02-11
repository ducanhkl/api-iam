package org.ducanh.apiiam.controllers;

import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.ducanh.apiiam.dto.requests.UserLoginRequestDto;
import org.ducanh.apiiam.dto.requests.UserRegisterRequestDto;
import org.ducanh.apiiam.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("register")
    public ResponseEntity<?> register(
            @RequestBody UserRegisterRequestDto userRegisterRequestDto,
            @RequestHeader(value = "namespace-id", required = true) Long namespaceId) {
        return ResponseEntity.ok()
                .body(authService.register(userRegisterRequestDto, namespaceId));
    }

    @PostMapping("login")
    public ResponseEntity<?> login(
            @RequestBody UserLoginRequestDto userLoginRequestDto,
            @RequestHeader("ip-address") String ipAddress,
            @RequestHeader("user-agent") String userAgent) {
        return ResponseEntity.ok().body(authService.login(userLoginRequestDto, ipAddress, userAgent));
    }

    @PutMapping("verify/{username}")
    public ResponseEntity<?> verify(@PathParam("username") String username,
                                    @RequestHeader("namespace-id") Long namespaceId) {
        authService.verify(username, namespaceId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("complete-verify/{username}")
    public ResponseEntity<?> completeVerify(
            @PathParam("username") String username,
            @RequestHeader("namespace-id") Long namespaceId,
            @RequestHeader("code") String code
    ) {
        authService.completeVerify(username, namespaceId, code);
        return ResponseEntity.ok().build();
    }
}
