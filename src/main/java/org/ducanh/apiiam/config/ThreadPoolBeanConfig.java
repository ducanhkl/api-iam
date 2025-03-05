package org.ducanh.apiiam.config;

import lombok.Data;
import lombok.NonNull;
import org.ducanh.apiiam.Constants;
import org.slf4j.MDC;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;


class MDCInheritableTaskDecorator implements TaskDecorator {
    @Override
    @NonNull
    public Runnable decorate(@NonNull Runnable runnable) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return () -> {
            MDC.setContextMap(contextMap);
            try {
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}

@Configuration
public class ThreadPoolBeanConfig {

    @Data
    public static class ThreadPoolConfiguration {
        @Name("core-pool-size")
        private int corePoolSize = 5;

        @Name("max-pool-size")
        private int maxPoolSize = 10;

        @Name("queue-capacity")
        private int queueCapacity = 100;

        @Name("keep-alive")
        private int keepAliveSeconds = 20;

        public ThreadPoolTaskExecutor configExecutor(ThreadPoolTaskExecutor threadPoolTaskExecutor) {
            threadPoolTaskExecutor.setCorePoolSize(corePoolSize);
            threadPoolTaskExecutor.setMaxPoolSize(maxPoolSize);
            threadPoolTaskExecutor.setQueueCapacity(queueCapacity);
            threadPoolTaskExecutor.setKeepAliveSeconds(keepAliveSeconds);
            return threadPoolTaskExecutor;
        }
    }

    @Bean(name = Constants.THREAD_EXECUTOR + "config")
    @ConfigurationProperties(prefix = "app.thread-pool.common")
    public ThreadPoolConfiguration threadPoolConfiguration() {
        return new ThreadPoolConfiguration();
    }

    @Bean(name = Constants.THREAD_EXECUTOR)
    public ThreadPoolTaskExecutor executorService(ThreadPoolConfiguration threadPoolConfiguration
    ) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setTaskDecorator(new MDCInheritableTaskDecorator());
        return threadPoolConfiguration.configExecutor(executor);
    }
}
