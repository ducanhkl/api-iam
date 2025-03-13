package org.ducanh.apiiam;

import com.redis.testcontainers.RedisContainer;
import groovy.util.logging.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
@Slf4j
public class ContainerConfig {

    private static final Logger log = LoggerFactory.getLogger(ContainerConfig.class);
    static String POSTGRES_IMAGE = "postgres:16-alpine";
    static String REDIS_IMAGE = "redis:alpine3.21";

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgres() {
        return new PostgreSQLContainer<>(POSTGRES_IMAGE);
    }

    @Bean
    @ServiceConnection
    public RedisContainer redis(DynamicPropertyRegistry registry) {
         return new RedisContainer(DockerImageName.parse(REDIS_IMAGE));
    }


}
