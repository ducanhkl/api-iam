package org.ducanh.apiiam.controllers;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.ducanh.apiiam.ContainerConfig;
import org.ducanh.apiiam.repositories.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(ContainerConfig.class)
public class CreateUserE2eTest {


    @Autowired
    private UserRepository userRepository;

    @LocalServerPort
    private int port;


    @BeforeEach
    public void beforeEach1() {
        RestAssured.port = port;
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
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
        userRepository.deleteAll();
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
}
