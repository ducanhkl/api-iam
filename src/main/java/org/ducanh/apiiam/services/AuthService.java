package org.ducanh.apiiam.services;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.ducanh.apiiam.Constants;
import org.ducanh.apiiam.dto.requests.UserLoginRequestDto;
import org.ducanh.apiiam.dto.requests.UserRegisterRequestDto;
import org.ducanh.apiiam.dto.responses.TokenRefreshResponse;
import org.ducanh.apiiam.dto.responses.UserLoginResponseDto;
import org.ducanh.apiiam.dto.responses.UserRegisterResponseDto;
import org.ducanh.apiiam.entities.*;
import org.ducanh.apiiam.helpers.TimeHelpers;
import org.ducanh.apiiam.repositories.NamespaceRepository;
import org.ducanh.apiiam.repositories.SessionRepository;
import org.ducanh.apiiam.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static org.ducanh.apiiam.helpers.ValidationHelpers.stringContainSpecialCharacters;
import static org.ducanh.apiiam.helpers.ValidationHelpers.valArg;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final NamespaceRepository namespaceRepository;
    private final OtpService otpService;
    private final JwtTokenService jwtTokenService;
    private final SessionService sessionService;
    private final SessionRepository sessionRepository;
    private final TimeHelpers timeHelpers;

    @Autowired
    public AuthService(UserRepository userRepository,
                       NamespaceRepository namespaceRepository,
                       OtpService otpService,
                       SessionService sessionService,
                       JwtTokenService jwtTokenService,
                       SessionRepository sessionRepository, TimeHelpers timeHelpers) {
        this.userRepository = userRepository;
        this.namespaceRepository = namespaceRepository;
        this.otpService = otpService;
        this.jwtTokenService = jwtTokenService;
        this.sessionService = sessionService;
        this.sessionRepository = sessionRepository;
        this.timeHelpers = timeHelpers;
    }

    public UserRegisterResponseDto register(UserRegisterRequestDto registerRequestDto,
                                            String namespaceId) {
        validate(registerRequestDto, namespaceId);
        checkRegisInfo(registerRequestDto, namespaceId);
        User user = createUserWithInitialStatus(registerRequestDto, namespaceId);
        return UserRegisterResponseDto.builder()
                .username(user.getUsername())
                .userId(user.getUserId())
                .namespaceId(user.getNamespaceId()).build();
    }

    public UserLoginResponseDto login(UserLoginRequestDto userLoginRequestDto,
                                      String ipAddress,
                                      String userAgent) {
        String namespaceId = userLoginRequestDto.namespaceId();
        validate(userLoginRequestDto);
        User user = userRepository.findByUsernameAndNamespaceId(userLoginRequestDto.username(), namespaceId);
        valArg(Objects.nonNull(user), () -> new RuntimeException("User not found"));
        valUserInfo(user, namespaceId);
        PasswordAlg alg = user.getPwdAlg();
        sessionService.checkMaxUserActiveSession(user);
        if (alg.compare(userLoginRequestDto.password(), user.getPasswordHash())) {
            user.setLastLogin(timeHelpers.currentTime());
            return jwtTokenService.issueJwtTokens(user, userAgent, ipAddress);
        }
        throw new RuntimeException("Invalid password");
    }

    public void verify(String username, String namespaceId) {
        User user = userRepository.findByUsernameAndNamespaceId(username, namespaceId);
        OTP otp = otpService.generateOtpForVerify(user);
        // implement to sent otp;
    }

    public void completeVerify(String username, String namespaceId,
                               String code) {
        User user = userRepository.findByUsernameAndNamespaceId(username, namespaceId);
        if (!otpService.completeVerifyOtp(user, code)) {
            throw new RuntimeException("Otp is wrong");
        }
    }

    public TokenRefreshResponse renewAccessToken(String refreshToken, String userAgent, String ipAddress) {
        DecodedJWT decodeRefreshToken = jwtTokenService.validateRefreshToken(refreshToken);
        String refreshTokenId = decodeRefreshToken.getId();
        Session session = sessionRepository.findSessionByRefreshTokenId(refreshTokenId);
        sessionService.revokeOldSession(session);
        User user = userRepository.findByUserId(Long.valueOf(decodeRefreshToken.getSubject()));
        UserLoginResponseDto result = jwtTokenService.issueJwtTokens(user, userAgent, ipAddress);
        return new TokenRefreshResponse(result.accessToken(), result.refreshToken());
    }

    public void logout(String refreshToken) {
        DecodedJWT decodeRefreshToken = jwtTokenService.validateRefreshToken(refreshToken);
        sessionService.deactivateSession(decodeRefreshToken.getId());
    }

    private User createUserWithInitialStatus(UserRegisterRequestDto request, String namespaceId) {
        String passwordHashed = Constants.DEFAULT_PASSWORD_ALG.hash(request.password());
        User user = User.initUser(request, passwordHashed, Constants.DEFAULT_PASSWORD_ALG, namespaceId);
        return userRepository.save(user);
    }

    private void valUserInfo(User user, String namespaceId) {
        // TO-DO: implement mechanism for limit the number of active session
        namespaceRepository.existOrThrowById(namespaceId);
        valArg(UserStatus.ACTIVE == user.getStatus(), () -> new RuntimeException("User status not valid"));
        valArg(!user.getAccountLocked(), () -> new RuntimeException("User account locked"));
        valArg(!user.getDeleted(), () -> new RuntimeException("User deleted"));
        valArg(Objects.nonNull(user.getPwdAlg()), () -> new RuntimeException("Password alg is empty"));
    }

    private void validate(UserLoginRequestDto request) {
        valArg(Objects.nonNull(request.username()), () -> new RuntimeException("Username cannot be null or empty"));
        valArg(Objects.nonNull(request.password()), () -> new RuntimeException("Password cannot be null or empty"));
        valArg(Objects.nonNull(request.namespaceId()), () -> new RuntimeException("NamespaceId cannot be null or empty"));
    }

    private void validate(UserRegisterRequestDto request, String namespaceId) {
        String username = request.username();
        String password = request.password();
        valArg(Objects.nonNull(request.username()), () -> new RuntimeException("Username cannot be null or empty"));
        valArg(Objects.nonNull(request.password()), () -> new RuntimeException("Password cannot be null or empty"));
        valArg(Objects.nonNull(request.email()), () -> new RuntimeException("Email cannot be null or empty"));
        valArg(Objects.nonNull(namespaceId), () -> new RuntimeException("Namespace ID cannot be null or empty"));
        valArg(Objects.nonNull(request.phoneNumber()), () -> new RuntimeException("Phone number cannot be null or empty"));
        valArg(username.length() > 3 && username.length() < 20, () -> new RuntimeException("Username must be between 3 and 100 characters"));
        valArg(password.length() > 8 && password.length() < 20, () -> new RuntimeException("Password must be between 8 and 100 characters"));
        valArg(stringContainSpecialCharacters(password), () -> new RuntimeException("Password must contain as least one special character"));
    }

    private void checkRegisInfo(UserRegisterRequestDto request, String namespaceId) {
        boolean isUsernameExisted = userRepository.existsByUsernameAndNamespaceId(request.username(), namespaceId);
        boolean isEmailExisted = userRepository.existsByEmailAndNamespaceId(request.email(), namespaceId);
        boolean namespaceExisted = namespaceRepository.existsById(namespaceId);
        valArg(!isUsernameExisted, () -> new RuntimeException("Username already existed"));
        valArg(!isEmailExisted, () -> new RuntimeException("Email already existed"));
        valArg(namespaceExisted, () -> new RuntimeException("Namespace not existed"));
    }
}
