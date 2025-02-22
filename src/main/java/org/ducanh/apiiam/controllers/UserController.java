package org.ducanh.apiiam.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ducanh.apiiam.dto.requests.CreateUserRequestDto;
import org.ducanh.apiiam.dto.requests.IndexUserRequestParamsDto;
import org.ducanh.apiiam.dto.requests.UpdatePasswordRequestDto;
import org.ducanh.apiiam.dto.requests.UpdateUserRequestDto;
import org.ducanh.apiiam.dto.responses.UserLoginResponseDto;
import org.ducanh.apiiam.dto.responses.UserResponseDto;
import org.ducanh.apiiam.services.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.ducanh.apiiam.Constants.*;

@RestController
@RequestMapping("user")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(
            @Valid @RequestBody CreateUserRequestDto requestDto) {
        log.info("Creating user: {}", requestDto);
        UserResponseDto response = userService.createUser(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("index")
    public ResponseEntity<List<UserResponseDto>> getUsers(
            IndexUserRequestParamsDto params
    ) {
        Pageable pageable = PageRequest.of(params.page(), params.size());
        Page<UserResponseDto> userPage = userService.indexUsers(params, pageable);

        return ResponseEntity.ok()
                .header(PAGE_NUMBER_HEADER, String.valueOf(userPage.getNumber()))
                .header(PAGE_SIZE_HEADER, String.valueOf(userPage.getSize()))
                .header(TOTAL_ELEMENTS_HEADER, String.valueOf(userPage.getTotalElements()))
                .header(TOTAL_PAGES_HEADER, String.valueOf(userPage.getTotalPages()))
                .body(userPage.getContent());
    }

    @GetMapping("/user-id/{userId}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @PutMapping("/user-id/{userId}")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequestDto requestDto) {
        return ResponseEntity.ok(userService.updateUser(userId, requestDto));
    }

    @DeleteMapping("/user-id/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/user-id/{userId}/password")
    public ResponseEntity<UserLoginResponseDto> updatePassword(
            @PathVariable Long userId,
            @Valid @RequestBody UpdatePasswordRequestDto requestDto,
            @RequestHeader("ip-address") String ipAddress,
            @RequestHeader("user-agent") String userAgent
    ) {
        return ResponseEntity.ok(userService.updatePassword(userId, requestDto, ipAddress, userAgent));
    }
}
