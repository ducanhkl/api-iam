package org.ducanh.apiiam.controllers;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.ducanh.apiiam.ContainerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(ContainerConfig.class)
public class CreateUserE2eTest {

    private Logger log = LoggerFactory.getLogger(CreateUserE2eTest.class);

    @LocalServerPort
    private int port;

    @BeforeEach
    public void beforeEach1() {
        RestAssured.port = port;
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }

    @Test
    public void testCreateUserSucceed() {
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
    }
}
