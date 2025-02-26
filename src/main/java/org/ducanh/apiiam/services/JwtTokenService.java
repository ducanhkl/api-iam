package org.ducanh.apiiam.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.ducanh.apiiam.dto.responses.UserLoginResponseDto;
import org.ducanh.apiiam.entities.JwtTokenType;
import org.ducanh.apiiam.entities.KeyPair;
import org.ducanh.apiiam.entities.Namespace;
import org.ducanh.apiiam.entities.User;
import org.ducanh.apiiam.exceptions.DomainException;
import org.ducanh.apiiam.exceptions.ErrorCode;
import org.ducanh.apiiam.helpers.TimeHelpers;
import org.ducanh.apiiam.repositories.KeyPairRepository;
import org.ducanh.apiiam.repositories.NamespaceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static org.ducanh.apiiam.Constants.*;

@Service
public class JwtTokenService {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private final KeyPairRepository keyPairRepository;
    private final SessionService sessionService;
    private final NamespaceRepository namespaceRepository;
    private final TimeHelpers timeHelpers;

    private final Duration accessTokenExpiration;
    private final Duration refreshTokenExpiration;

    public JwtTokenService(final KeyPairRepository keyPairRepository,
                           final SessionService sessionService,
                           final NamespaceRepository namespaceRepository,
                           final TimeHelpers timeHelpers,
                           @Value("5") Integer accessTokenExpirationInSecond,
                           @Value("10") Integer refreshTokenExpirationInDay) {
        this.keyPairRepository = keyPairRepository;
        this.sessionService = sessionService;
        this.namespaceRepository = namespaceRepository;
        this.timeHelpers = timeHelpers;
        accessTokenExpiration = Duration.ofSeconds(accessTokenExpirationInSecond);
        refreshTokenExpiration = Duration.ofDays(refreshTokenExpirationInDay);
    }

    public UserLoginResponseDto issueJwtTokens(User user, String userAgent, String ipAddress) {
        Namespace namespace = namespaceRepository.findByNamespaceId(user.getNamespaceId());
        KeyPair keyPair = keyPairRepository.findKeyPairsByKeyPairId(namespace.getKeyPairId());
        String accessTokenId = UUID.randomUUID().toString();
        String refreshTokenId = UUID.randomUUID().toString();
        OffsetDateTime currentTime = timeHelpers.currentTime();
        OffsetDateTime refreshTokenExpireAt = currentTime.plus(refreshTokenExpiration);
        String accessToken = generateAccessToken(user,  keyPair, accessTokenId, currentTime);
        String refreshToken = generateRefreshToken(user, keyPair, refreshTokenId, currentTime, refreshTokenExpireAt);
        sessionService.createSession(user, keyPair, userAgent, ipAddress, accessTokenId, refreshTokenId,
                currentTime, refreshTokenExpireAt);
        return new UserLoginResponseDto(refreshToken, accessToken);
    }

    public DecodedJWT validateRefreshToken(String refreshToken) {
        final DecodedJWT decodedJWT;
        try {
            decodedJWT = JWT.decode(refreshToken);
        } catch (Exception ex) {
            throw new DomainException(ErrorCode.INVALID_TOKEN, "Invalid refresh token")
                    .setCause(ex);
        }
        KeyPair keyPair = keyPairRepository.findKeyPairsByKeyPairId(Long.valueOf(decodedJWT.getKeyId()));
        String tokenType = Optional.of(decodedJWT.getClaim(TOKEN_TYPE))
                .map(Claim::asString)
                .orElseThrow(() -> new RuntimeException("Token type not existed"));
        if (!tokenType.equals(JwtTokenType.REFRESH_TOKEN.toString())) {
            throw new RuntimeException("Refresh token does not match expected token type");
        }
        try {
            return JWT.require(getVerifyAlgorithm(keyPair))
                    .build()
                    .verify(refreshToken);
        } catch (Exception ex) {
            throw new RuntimeException("Verify refreshToken failed", ex);
        }
    }

    private String generateRefreshToken(User user, KeyPair keyPair, String id, OffsetDateTime currentTime,
                                        OffsetDateTime expireAt) {
        return JWT.create()
                .withJWTId(id) // Unique JTI
                .withSubject(user.getUserId().toString())
                .withClaim(TOKEN_TYPE, JwtTokenType.REFRESH_TOKEN.toString())
                .withIssuedAt(currentTime.toInstant())
                .withExpiresAt(expireAt.toInstant())
                .withKeyId(String.valueOf(keyPair.getKeyPairId()))
                .withIssuer(DEFAULT_ISSUER)
                .sign(getSigningAlgorithm(keyPair));
    }

    private String generateAccessToken(User user, KeyPair keyPair, String id, OffsetDateTime currentTime) {
        // TO-DO: Add groups of user
        Instant expireAt = currentTime.plus(accessTokenExpiration).toInstant();
        return JWT.create()
                .withJWTId(id) // Unique JTI
                .withSubject(user.getUserId().toString())
                .withIssuedAt(currentTime.toInstant())
                .withExpiresAt(expireAt)
                .withClaim(TOKEN_TYPE_NAME, JwtTokenType.ACCESS_TOKEN.toString())
                .withClaim(NAMESPACE_NAME, user.getNamespaceId())
                .withClaim(EMAIL_NAME, user.getEmail())
                .withKeyId(String.valueOf(keyPair.getKeyPairId()))
                .withIssuer(DEFAULT_ISSUER)
                .sign(getSigningAlgorithm(keyPair));
    }

    private Algorithm getSigningAlgorithm(KeyPair keyPair) {
        if (keyPair.getAlgorithm() != KeyPair.Algorithm.RSA) {
            throw new RuntimeException("Key algorithm is not RSA");
        }
        String privateKeyPEM = keyPair.getEncryptedPrivateKey()
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END RSA PRIVATE KEY-----", "");
        byte[] privateKey = Base64.getDecoder().decode(privateKeyPEM);
        try {
            RSAPrivateKey rsaPrivateKey  = (RSAPrivateKey) KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(privateKey));
            return Algorithm.RSA256(null, rsaPrivateKey);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    private Algorithm getVerifyAlgorithm(KeyPair keyPair) {
        if (keyPair.getAlgorithm() != KeyPair.Algorithm.RSA) {
            throw new RuntimeException("Key algorithm is not RSA");
        }
        String publicKeyPEM = keyPair.getPublicKey()
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PUBLIC KEY-----", "");
        try {
            RSAPublicKey rsaPublicKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyPEM)));
            return Algorithm.RSA256(rsaPublicKey, null);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}