package org.ducanh.apiiam.services;

import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ducanh.apiiam.dto.requests.CreateUserRequestDto;
import org.ducanh.apiiam.dto.requests.IndexUserRequestParamsDto;
import org.ducanh.apiiam.dto.requests.UpdateUserRequestDto;
import org.ducanh.apiiam.dto.responses.CreateUserResponseDto;
import org.ducanh.apiiam.dto.responses.GetUserResponseDto;
import org.ducanh.apiiam.dto.responses.UpdateUserResponseDto;
import org.ducanh.apiiam.entities.PasswordAlg;
import org.ducanh.apiiam.entities.User;
import org.ducanh.apiiam.entities.UserStatus;
import org.ducanh.apiiam.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;


    @Transactional
    public CreateUserResponseDto createUser(CreateUserRequestDto request) {
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
        return savedUser.toCreateUserResponse();
    }


    public Page<GetUserResponseDto> indexUsers(IndexUserRequestParamsDto params, Pageable pageable) {
        return userRepository.findAll(
                buildSearchCriteria(params),
                pageable
        ).map(User::toGetUserResponse);
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
    public GetUserResponseDto getUser(Long id) {
        return userRepository.findById(id)
                .map(User::toGetUserResponse)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Transactional
    public UpdateUserResponseDto updateUser(Long id, UpdateUserRequestDto request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber());
        user.setMfaEnabled(request.mfaEnabled());
        user.setAccountLocked(request.accountLocked());
        return user.toUpdateUserResponse();
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setDeleted(true);
    }

}
