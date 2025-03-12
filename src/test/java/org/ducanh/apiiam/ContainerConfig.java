package org.ducanh.apiiam;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class ContainerConfig {

    static String POSTGRES_IMAGE = "postgres:16-alpine";
    static String REDIS_IMAGE = "redis:alpine3.21";

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgres() {
        return new PostgreSQLContainer<>(POSTGRES_IMAGE);
    }

    @Bean
    @ServiceConnection
    public RedisContainer redis() {
        return new RedisContainer(DockerImageName.parse(REDIS_IMAGE));
    }

}
