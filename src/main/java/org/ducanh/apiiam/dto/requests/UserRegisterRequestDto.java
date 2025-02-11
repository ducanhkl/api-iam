package org.ducanh.apiiam.dto.requests;


public record UserRegisterRequestDto(String username, String password,
                                     String email, String phoneNumber) {}