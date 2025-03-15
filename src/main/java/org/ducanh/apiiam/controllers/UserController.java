package org.ducanh.apiiam.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "User Controller", description = "Operations for managing user accounts")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create new user",
            description = "Creates a new user account with the provided details")
    public ResponseEntity<UserResponseDto> createUser(
            @Valid @RequestBody CreateUserRequestDto requestDto) {
        log.info("Creating user: {}", requestDto);
        UserResponseDto response = userService.createUser(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("index")
    @Operation(summary = "List users",
            description = "Retrieves a paginated list of users with optional filtering")
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
    @Operation(summary = "Get user details",
            description = "Retrieves details of a specific user by their ID")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @PutMapping("/user-id/{userId}")
    @Operation(summary = "Update user details",
            description = "Updates the details of an existing user")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequestDto requestDto) {
        return ResponseEntity.ok(userService.updateUser(userId, requestDto));
    }

    @DeleteMapping("/user-id/{userId}")
    @Operation(summary = "Delete user",
            description = "Deletes a user account by their ID")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/user-id/{userId}/password")
    @Operation(summary = "Update user password",
            description = "Updates a user's password and returns new login credentials")
    public ResponseEntity<UserLoginResponseDto> updatePassword(
            @PathVariable Long userId,
            @Valid @RequestBody UpdatePasswordRequestDto requestDto,
            @RequestHeader("ip-address") String ipAddress,
            @RequestHeader("user-agent") String userAgent
    ) {
        return ResponseEntity.ok(userService.updatePassword(userId, requestDto, ipAddress, userAgent));
    }
}