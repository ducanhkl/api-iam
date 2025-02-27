package org.ducanh.apiiam.services;

import org.ducanh.apiiam.entities.KeyPair;
import org.ducanh.apiiam.entities.Namespace;
import org.ducanh.apiiam.entities.User;
import org.ducanh.apiiam.exceptions.CommonException;
import org.ducanh.apiiam.helpers.TimeHelpers;
import org.ducanh.apiiam.repositories.KeyPairRepository;
import org.ducanh.apiiam.repositories.NamespaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.security.KeyPairGenerator;
import java.time.OffsetDateTime;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class JwtTokenServiceTest {
    @Mock
    private KeyPairRepository keyPairRepository;

    @Mock
    private NamespaceRepository namespaceRepository;

    @Mock
    private SessionService sessionService;

    @Mock
    private TimeHelpers timeHelpers;

    private JwtTokenService jwtTokenService;

    private KeyPair testKeyPair;
    private User testUser;
    private Namespace testNamespace;

    @BeforeEach
    void setUp() throws Exception {
        jwtTokenService = new JwtTokenService(
                keyPairRepository,
                sessionService,
                namespaceRepository,
                timeHelpers,
                5,  // accessTokenExpirationInSecond
                10  // refreshTokenExpirationInDay
        );
        // Generate real RSA key pair for testing
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        java.security.KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // Create test KeyPair entity
        testKeyPair = KeyPair.builder()
                .keyPairId(1L)
                .algorithm(KeyPair.Algorithm.RSA)
                .publicKey("-----BEGIN PUBLIC KEY-----\n" +
                        Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()) +
                        "\n-----END PUBLIC KEY-----")
                .encryptedPrivateKey("-----BEGIN RSA PRIVATE KEY-----\n" +
                        Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()) +
                        "\n-----END RSA PRIVATE KEY-----")
                .isActive(true)
                .keyStatus(KeyPair.KeyStatus.ACTIVE)
                .keyUsage(KeyPair.KeyUsage.SIGNATURE)
                .build();

        // Create test User
        testUser = User.builder()
                .userId(1L)
                .username("testuser")
                .email("test@example.com")
                .namespaceId("test-namespace")
                .build();

        // Create test Namespace
        testNamespace = Namespace.builder()
                .namespaceId("test-namespace")
                .keyPairId(1L)
                .build();

        // Mock time
        when(timeHelpers.currentTime()).thenReturn(OffsetDateTime.now());
    }

    @Test
    void whenValidatingRefreshTokenWithInvalidKey_thenThrowException() {
        // Arrange
        when(namespaceRepository.findByNamespaceId(testUser.getNamespaceId())).thenReturn(testNamespace);
        when(keyPairRepository.findKeyPairsByKeyPairId(anyLong())).thenReturn(testKeyPair);

        // Generate a valid token first
        var tokens = jwtTokenService.issueJwtTokens(testUser, "test-agent", "127.0.0.1");

        // Now corrupt the key pair
        testKeyPair.setPublicKey("invalid-public-key");
        // Act & Assert
        CommonException exception = assertThrows(CommonException.class,
                () -> jwtTokenService.validateRefreshToken(tokens.refreshToken()));

        assertEquals("Verify refreshToken failed", exception.longDescription);
    }

    @Test
    void whenGeneratingTokenWithInvalidPrivateKey_thenThrowSpecificException() {
        // Arrange
        when(namespaceRepository.findByNamespaceId(testUser.getNamespaceId())).thenReturn(testNamespace);
        when(keyPairRepository.findKeyPairsByKeyPairId(anyLong())).thenReturn(testKeyPair);

        // Corrupt the private key format
        testKeyPair.setEncryptedPrivateKey("invalid-private-key");

        // Act & Assert
        CommonException exception = assertThrows(CommonException.class,
                () -> jwtTokenService.issueJwtTokens(testUser, "test-agent", "127.0.0.1"));
        assertEquals("Generate private key failed", exception.longDescription);
    }
}
