package org.ducanh.apiiam.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CreateUserE2eTest {

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
    }


}
