package org.ducanh.apiiam;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(ContainerConfig.class)
class ApiIamApplicationTests {

    @Test
    void contextLoads() {
    }

}
