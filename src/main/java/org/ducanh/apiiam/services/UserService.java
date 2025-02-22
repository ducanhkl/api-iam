package org.ducanh.apiiam.services;

import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ducanh.apiiam.dto.requests.CreateUserRequestDto;
import org.ducanh.apiiam.dto.requests.IndexUserRequestParamsDto;
import org.ducanh.apiiam.dto.requests.UpdatePasswordRequestDto;
import org.ducanh.apiiam.dto.requests.UpdateUserRequestDto;
import org.ducanh.apiiam.dto.responses.UserLoginResponseDto;
import org.ducanh.apiiam.dto.responses.UserResponseDto;
import org.ducanh.apiiam.entities.PasswordAlg;
import org.ducanh.apiiam.entities.User;
import org.ducanh.apiiam.entities.UserStatus;
import org.ducanh.apiiam.repositories.SessionRepository;
import org.ducanh.apiiam.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static org.ducanh.apiiam.helpers.ValidationHelpers.valArg;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final SessionService sessionService;
    private final JwtTokenService jwtTokenService;

    public UserService(UserRepository userRepository,
                       SessionService sessionService,
                       JwtTokenService jwtTokenService
                       ) {
        this.userRepository = userRepository;
        this.sessionService = sessionService;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public UserResponseDto createUser(CreateUserRequestDto request) {
        PasswordAlg passwordAlg = PasswordAlg.BCRYPT;
        String hashedPassword = passwordAlg.hash(request.password());

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(hashedPassword)
                .pwdAlg(passwordAlg)
                .namespaceId(request.namespaceId())
                .isVerified(request.isVerified())
                .deleted(false)
                .status(request.status() != null ? request.status() : UserStatus.ACTIVE)
                .mfaEnabled(request.mfaEnabled() != null ? request.mfaEnabled() : false)
                .accountLocked(false)
                .phoneNumber(request.phoneNumber())
                .build();

        User savedUser = userRepository.save(user);
        return savedUser.toUserResponseDto();
    }


    public Page<UserResponseDto> indexUsers(IndexUserRequestParamsDto params, Pageable pageable) {
        return userRepository.findAll(
                buildSearchCriteria(params),
                pageable
        ).map(User::toUserResponseDto);
    }

    private Specification<User> buildSearchCriteria(IndexUserRequestParamsDto params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get(User.Fields.deleted), false));
            if (params.userId() != null) {
                predicates.add(cb.equal(root.get(User.Fields.userId), params.userId()));
            }
            if (StringUtils.hasText(params.username())) {
                predicates.add(cb.equal(
                        root.get(User.Fields.username),
                        params.username().toLowerCase().trim()
                ));
            }
            if (StringUtils.hasText(params.email())) {
                predicates.add(cb.equal(
                        cb.lower(root.get(User.Fields.email)),
                        params.email().toLowerCase().trim()
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
    public UserResponseDto getUser(Long userId) {
        return userRepository.findById(userId)
                .map(User::toUserResponseDto)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    @Transactional
    public UserResponseDto updateUser(Long userId, UpdateUserRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber());
        user.setMfaEnabled(request.mfaEnabled());
        user.setAccountLocked(request.accountLocked());
        return user.toUserResponseDto();
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.setDeleted(true);
    }

    public UserLoginResponseDto updatePassword(Long userId,
                                               UpdatePasswordRequestDto updatePasswordRequestDto,
                                               String userAgent,
                                               String ipAddress) {
        User user = userRepository.findByUserIdOrThrow(userId);
        valUserInfoForChangePassword(user);
        PasswordAlg passwordAlg = user.getPwdAlg();
        if (!passwordAlg.compare(updatePasswordRequestDto.oldPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Old password does not match");
        }
        String hashed = passwordAlg.hash(updatePasswordRequestDto.newPassword());
        user.setPasswordHash(hashed);
        if (updatePasswordRequestDto.isLogoutOtherSession()) {
            sessionService.deactiveAllActiveSessions(user);
        }
        return jwtTokenService.issueJwtTokens(user, userAgent, ipAddress);
    }

    private void valUserInfoForChangePassword(User user) {
        valArg(!user.getDeleted(), () -> new RuntimeException("User deleted"));
        valArg(user.getIsVerified(), () -> new RuntimeException("User not verified"));
    }

}
