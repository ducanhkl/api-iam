package org.ducanh.apiiam.controllers;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.ducanh.apiiam.ContainerConfig;
import org.ducanh.apiiam.entities.OTP;
import org.ducanh.apiiam.entities.User;
import org.ducanh.apiiam.repositories.OtpRepository;
import org.ducanh.apiiam.repositories.SessionRepository;
import org.ducanh.apiiam.repositories.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.shaded.org.apache.commons.lang3.tuple.Pair;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(ContainerConfig.class)
public class AuthControllerE2eTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private OtpRepository otpRepository;

    @LocalServerPort
    private int port;


    @BeforeEach
    public void beforeEach1() {
        RestAssured.port = port;
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        userRepository.deleteAll();
        sessionRepository.deleteAll();
        otpRepository.deleteAll();
    }

    private String getLatestOtpCode(String username, String namespaceId) {
        User user = userRepository.findByUsernameAndNamespaceId(username, namespaceId);
        OTP otp = otpRepository.findLatestOTPByUserIdAndType(user.getUserId(), OTP.Type.VERIFY);
        return otp.getCode();
    }


    @Test
    public void givenUser_whenRegister_shouldReturnSucceed() {
        String requestBodyToCreateUser = """
                {
                  "username": "ducanh",
                  "password": "ducanh123!",
                  "email": "chuducanh@gmail.com",
                  "phoneNumber": "0936609206"
                }
                """;
        given().contentType(ContentType.JSON)
                .body(requestBodyToCreateUser)
                .header("namespace-id", "master")
                .when().post("auth/register")
                .then()
                .statusCode(200);
        long count = userRepository.count();
        Assertions.assertEquals(1, count);
    }

    @Test
    public void givenUserAndWrongNamespace_whenRegister_shouldReturnError() {
        String requestBodyToCreateUser = """
                {
                  "username": "ducanh",
                  "password": "ducanh123!",
                  "email": "chuducanh@gmail.com",
                  "phoneNumber": "0936609206"
                }
                """;
        given().contentType(ContentType.JSON)
                .body(requestBodyToCreateUser)
                .header("namespace-id", "not-exist-namespace")
                .when().post("auth/register")
                .then()
                .statusCode(400);
        long count = userRepository.count();
        Assertions.assertEquals(0, count);
    }

    @Test
    void whenRegisterWithExistingUsername_thenFail() {
        // First registration
        given()
                .contentType(ContentType.JSON)
                .body("""
                {
                    "username": "duplicate",
                    "password": "duplicate123!",
                    "email": "duplicate@test.com",
                    "phoneNumber": "0936609206"
                }
                """)
                .header("namespace-id", "master")
                .post("/auth/register");

        // Second registration with same username
        given()
                .contentType(ContentType.JSON)
                .body("""
                {
                    "username": "duplicate",
                    "password": "duplicate123!",
                    "email": "different@test.com",
                    "phoneNumber": "0936609207"
                }
                """)
                .header("namespace-id", "master")
                .when()
                .post("/auth/register")
                .then()
                .statusCode(400)
                .body("errorCode", containsString("UNKNOWN_002_400"));
    }

    private Pair<String, String> registerAndVerifyUser(String username, String password, String email, String phone) {
        // Register
        given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                {
                    "username": "%s",
                    "password": "%s",
                    "email": "%s",
                    "phoneNumber": "%s"
                }
                """, username, password, email, phone))
                .header("namespace-id", "master")
                .post("/auth/register");

        // Verify
        given()
                .header("namespace-id", "master")
                .put("/auth/verify/" + username);

        String otpCode = getLatestOtpCode(username, "master");

        given()
                .header("namespace-id", "master")
                .header("code", otpCode)
                .put("/auth/complete-verify/" + username);

        // Login and return refresh token
        var json = given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                {
                    "username": "%s",
                    "password": "%s",
                    "namespaceId": "master"
                }
                """, username, password))
                .header("ip-address", "127.0.0.1")
                .header("user-agent", "test-agent")
                .post("/auth/login")
                .jsonPath();
        return Pair.of(json.getString("accessToken"), json.getString("refreshToken"));
    }

    @Test
    void whenLoginWithValidCredentials_thenSuccess() {
        registerAndVerifyUser("login", "login123!", "login@test.com", "0936609206");

        given()
                .contentType(ContentType.JSON)
                .body("""
                {
                    "username": "login",
                    "password": "login123!",
                    "namespaceId": "master"
                }
                """)
                .header("ip-address", "127.0.0.1")
                .header("user-agent", "test-agent")
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());
    }

    @Test
    void whenLoginWithInvalidPassword_thenFail() {
        registerAndVerifyUser("wrongpass", "correct123!", "wrongpass@test.com", "0936609206");

        given()
                .contentType(ContentType.JSON)
                .body("""
                {
                    "username": "wrongpass",
                    "password": "wrong123!",
                    "namespaceId": "master"
                }
                """)
                .header("ip-address", "127.0.0.1")
                .header("user-agent", "test-agent")
                .when()
                .post("/auth/login")
                .then()
                .statusCode(401);
    }

    @Test
    void whenRefreshTokenWithValidToken_thenSuccess() {
        Pair<String, String> token = registerAndVerifyUser("refresh", "refresh123!", "refresh@test.com", "0936609206");
        String refreshToken = token.getValue();
        given()
                .header("refresh-token", refreshToken)
                .header("user-agent", "test-agent")
                .header("ip-address", "127.0.0.1")
                .when()
                .put("/auth/token/refresh")
                .then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());
    }

    @Test
    void whenRefreshTokenWithInvalidToken_thenFail() {
        given()
                .header("refresh-token", "invalid-token")
                .header("user-agent", "test-agent")
                .header("ip-address", "127.0.0.1")
                .when()
                .put("/auth/token/refresh")
                .then()
                .statusCode(401);
    }

    @Test
    void whenLogoutWithValidToken_thenSuccess() {
        // Given - Register, verify and login
        Pair<String, String> tokens = registerAndVerifyUser("logout", "logout123!", "logout@test.com", "0936609206");
        String refreshToken = tokens.getValue();

        // When - Logout
        given()
                .header("refresh-token", refreshToken)
                .when()
                .delete("/auth/logout")
                .then()
                .statusCode(200);

        // Then - Verify session is deactivated
        long activeSessionCount = sessionRepository.countSessionByUserId(
                userRepository.findByUsernameAndNamespaceId("logout", "master").getUserId(),
                true
        );
        Assertions.assertEquals(0, activeSessionCount);
    }


    @Test
    void whenLoginWithUnverifiedUser_thenFail() {
        // Given - Register without verification
        given()
                .contentType(ContentType.JSON)
                .body("""
            {
                "username": "unverified",
                "password": "unverified123!",
                "email": "unverified@test.com",
                "phoneNumber": "0936609206"
            }
            """)
                .header("namespace-id", "master")
                .post("/auth/register");

        // When - Try to login
        given()
                .contentType(ContentType.JSON)
                .body("""
            {
                "username": "unverified",
                "password": "unverified123!",
                "namespaceId": "master"
            }
            """)
                .header("ip-address", "127.0.0.1")
                .header("user-agent", "test-agent")
                .when()
                .post("/auth/login")
                .then()
                .statusCode(401)
                .body("errorCode", containsString("USER_006_401"));
    }


    @Test
    void whenCompleteVerifyWithInvalidOTP_thenFail() {
        // Given - Register and initiate verification
        given()
                .contentType(ContentType.JSON)
                .body("""
            {
                "username": "verify",
                "password": "verify123!",
                "email": "verify@test.com",
                "phoneNumber": "0936609206"
            }
            """)
                .header("namespace-id", "master")
                .post("/auth/register");

        given()
                .header("namespace-id", "master")
                .put("/auth/verify/verify");

        // When - Try to verify with wrong OTP
        given()
                .header("namespace-id", "master")
                .header("code", "000000")
                .when()
                .put("/auth/complete-verify/verify")
                .then()
                .statusCode(400)
                .body("errorCode", containsString("OTP_005_400"));
    }

    @Test
    void whenLoginWithNonExistentUsername_thenFail() {
        given()
                .contentType(ContentType.JSON)
                .body("""
            {
                "username": "nonexistent",
                "password": "password123!",
                "namespaceId": "master"
            }
            """)
                .header("ip-address", "127.0.0.1")
                .header("user-agent", "test-agent")
                .when()
                .post("/auth/login")
                .then()
                .statusCode(400)
                .body("errorCode", containsString("USER_004_400"))
                .body("shortDescriptions", containsString("Username not existed"));
    }

    @Test
    void whenRenewTokenAndPassWrongTokenType_shouldThrowError() {
        // Given - Register, verify and login
        Pair<String, String> tokens = registerAndVerifyUser("logout", "logout123!", "logout@test.com", "0936609206");
        String accessToken = tokens.getKey();

        // When - Logout
        given()
                .header("refresh-token", accessToken) // Wrong token
                .when()
                .delete("/auth/logout")
                .then()
                .statusCode(401)
                .body("errorCode", containsString("TOKEN_008_401"));
        // Then - Verify session is deactivated
        long activeSessionCount = sessionRepository.countSessionByUserId(
                userRepository.findByUsernameAndNamespaceId("logout", "master").getUserId(),
                true
        );
        Assertions.assertEquals(1, activeSessionCount);
    }

    @Test
    void whenRegisterWithInvalidEmailFormat_thenFail() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                {
                    "username": "invalidemail",
                    "password": "password123!",
                    "email": "invalid-email",
                    "phoneNumber": "0936609206"
                }
                """)
                .header("namespace-id", "master")
                .when()
                .post("/auth/register")
                .then()
                .statusCode(400)
                .body("errorCode", containsString("VALIDATION_002_400"));
    }

    @Test
    void whenRegisterWithWeakPassword_thenFail() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                {
                    "username": "weakpass",
                    "password": "123",
                    "email": "weak@test.com",
                    "phoneNumber": "0936609206"
                }
                """)
                .header("namespace-id", "master")
                .when()
                .post("/auth/register")
                .then()
                .statusCode(400)
                .body("errorCode", containsString("PASSWORD_007_400"));
    }

    @Test
    void whenVerifyWithNonExistentUsername_thenFail() {
        given()
                .header("namespace-id", "master")
                .when()
                .put("/auth/verify/nonexistent")
                .then()
                .statusCode(400)
                .body("errorCode", containsString("USER_004_400"));
    }

    @Test
    void whenCompleteVerifyWithExpiredOTP_thenFail() throws InterruptedException {
        // Given - Register and initiate verification
        given()
                .contentType(ContentType.JSON)
                .body("""
                {
                    "username": "expiredotp",
                    "password": "expired123!",
                    "email": "expired@test.com",
                    "phoneNumber": "0936609206"
                }
                """)
                .header("namespace-id", "master")
                .post("/auth/register");

        given()
                .header("namespace-id", "master")
                .put("/auth/verify/expiredotp");

        // Wait for OTP to expire (assuming 1 minute expiration)
        Thread.sleep(61000);

        // When - Try to verify with expired OTP
        String otpCode = getLatestOtpCode("expiredotp", "master");
        given()
                .header("namespace-id", "master")
                .header("code", otpCode)
                .when()
                .put("/auth/complete-verify/expiredotp")
                .then()
                .statusCode(400)
                .body("errorCode", containsString("OTP_005_400"));
    }

    @Test
    void whenLoginWithMultipleSessions_thenSuccess() {
        // Given - Register and verify user
        registerAndVerifyUser("multisession", "multisession123!", "multisession@test.com", "0936609206");

        // When - Create multiple sessions
        for (int i = 0; i < 3; i++) {
            given()
                    .contentType(ContentType.JSON)
                    .body("""
                    {
                        "username": "multisession",
                        "password": "multisession123!",
                        "namespaceId": "master"
                    }
                    """)
                    .header("ip-address", "127.0.0." + i)
                    .header("user-agent", "test-agent-" + i)
                    .post("/auth/login");
        }

        // Then - Verify all sessions are active
        long activeSessionCount = sessionRepository.countSessionByUserId(
                userRepository.findByUsernameAndNamespaceId("multisession", "master").getUserId(),
                true
        );
        Assertions.assertEquals(3, activeSessionCount);
    }

    @Test
    void whenLogoutAllSessions_thenSuccess() {
        // Given - Register, verify and create multiple sessions
        Pair<String, String> tokens = registerAndVerifyUser("logoutall", "logoutall123!", "logoutall@test.com", "0936609206");
        for (int i = 0; i < 3; i++) {
            given()
                    .contentType(ContentType.JSON)
                    .body("""
                    {
                        "username": "logoutall",
                        "password": "logoutall123!",
                        "namespaceId": "master"
                    }
                    """)
                    .header("ip-address", "127.0.0." + i)
                    .header("user-agent", "test-agent-" + i)
                    .post("/auth/login");
        }

        // When - Logout all sessions
        given()
                .header("refresh-token", tokens.getValue())
                .when()
                .delete("/auth/logout-all")
                .then()
                .statusCode(200);

        // Then - Verify no active sessions
        long activeSessionCount = sessionRepository.countSessionByUserId(
                userRepository.findByUsernameAndNamespaceId("logoutall", "master").getUserId(),
                true
        );
        Assertions.assertEquals(0, activeSessionCount);
    }
}
