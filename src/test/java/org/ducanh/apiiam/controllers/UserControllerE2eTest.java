package org.ducanh.apiiam.controllers;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.ducanh.apiiam.ContainerConfig;
import org.ducanh.apiiam.entities.PasswordAlg;
import org.ducanh.apiiam.entities.User;
import org.ducanh.apiiam.entities.UserStatus;
import org.ducanh.apiiam.repositories.SessionRepository;
import org.ducanh.apiiam.repositories.UserRepository;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(ContainerConfig.class)
public class UserControllerE2eTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private Flyway flyway;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        sessionRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User createTestUser(String username, String email) {
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(PasswordAlg.BCRYPT.hash("password123!"))
                .pwdAlg(PasswordAlg.BCRYPT)
                .namespaceId("master")
                .isVerified(true)
                .deleted(false)
                .status(UserStatus.ACTIVE)
                .mfaEnabled(false)
                .accountLocked(false)
                .phoneNumber("1234567890")
                .build();
        return userRepository.save(user);
    }

    @Test
    void shouldCreateUserSuccessfully() {
        flyway.clean();
        flyway.migrate();
        String requestBody = """
            {
                "username": "testuser",
                "email": "test@example.com",
                "password": "password123!",
                "namespaceId": "test-namespace",
                "isVerified": true,
                "status": "ACTIVE",
                "mfaEnabled": false,
                "phoneNumber": "1234567890"
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/user")
                .then()
                .statusCode(201)
                .body("username", equalTo("testuser"))
                .body("email", equalTo("test@example.com"))
                .body("isVerified", equalTo(true))
                .body("status", equalTo("ACTIVE"))
                .body("mfaEnabled", equalTo(false))
                .body("phoneNumber", equalTo("1234567890"));
    }

    @Test
    void shouldGetUserSuccessfully() {
        User user = createTestUser("testuser", "test@example.com");

        given()
                .when()
                .get("/user/user-id/{userId}", user.getUserId())
                .then()
                .statusCode(200)
                .body("userId", equalTo(user.getUserId().intValue()))
                .body("username", equalTo(user.getUsername()))
                .body("email", equalTo(user.getEmail()));
    }

    @Test
    void shouldFailWhenGettingNonExistentUser() {
        given()
                .when()
                .get("/user/user-id/{userId}", 999999L)
                .then()
                .statusCode(400)
                .body("errorCode", equalTo("USER_016_400"));
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        User user = createTestUser("testuser", "test@example.com");

        String requestBody = """
            {
                "username": "updateduser",
                "email": "updated@example.com",
                "phoneNumber": "9876543210",
                "mfaEnabled": true,
                "accountLocked": false
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/user/user-id/{userId}", user.getUserId())
                .then()
                .statusCode(200)
                .body("username", equalTo("updateduser"))
                .body("email", equalTo("updated@example.com"))
                .body("phoneNumber", equalTo("9876543210"))
                .body("mfaEnabled", equalTo(true));
    }

    @Test
    void shouldDeleteUserSuccessfully() {
        User user = createTestUser("testuser", "test@example.com");

        given()
                .when()
                .delete("/user/user-id/{userId}", user.getUserId())
                .then()
                .statusCode(204);

        User deletedUser = userRepository.findById(user.getUserId()).orElseThrow();
        assertTrue(deletedUser.getDeleted());
    }

    @Test
    void shouldUpdatePasswordSuccessfully() {
        flyway.clean();
        flyway.migrate();
        User user = createTestUser("testuser", "test@example.com");

        String requestBody = """
            {
                "oldPassword": "password123!",
                "newPassword": "newpassword123!",
                "isLogoutOtherSession": true
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .header("ip-address", "127.0.0.1")
                .header("user-agent", "test-agent")
                .body(requestBody)
                .when()
                .patch("/user/user-id/{userId}/password", user.getUserId())
                .then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());
    }

    @Test
    void shouldFailWhenUpdatingPasswordWithIncorrectOldPassword() {
        User user = createTestUser("testuser", "test@example.com");

        String requestBody = """
            {
                "oldPassword": "wrongpassword",
                "newPassword": "newpassword123!",
                "isLogoutOtherSession": true
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .header("ip-address", "127.0.0.1")
                .header("user-agent", "test-agent")
                .body(requestBody)
                .when()
                .patch("/user/user-id/{userId}/password", user.getUserId())
                .then()
                .statusCode(401);
    }

    @Test
    void shouldListUsersWithPagination() {
        List<User> users = List.of(
                createTestUser("user1", "user1@example.com"),
                createTestUser("user2", "user2@example.com")
        );

        given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/user/index")
                .then()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("findAll { it.username =~ /user.*/ }", hasSize(2));
    }

    @Test
    void shouldFilterUsersByUsername() {
        createTestUser("admin", "admin@example.com");
        createTestUser("user", "user@example.com");

        given()
                .queryParam("username", "admin")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/user/index")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].username", equalTo("admin"));
    }

    @Test
    void shouldFilterUsersByEmail() {
        createTestUser("user1", "admin@example.com");
        createTestUser("user2", "user@example.com");

        given()
                .queryParam("email", "admin@example.com")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/user/index")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].email", equalTo("admin@example.com"));
    }
}