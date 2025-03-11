package org.ducanh.apiiam.listeners.redis;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class NamespaceVersionChangeListener implements MessageListener {



//    @EventListener(ApplicationReadyEvent.class)
//    public void onApplicationStart() {
//        var executor = Executors.newScheduledThreadPool(1);
//        executor.scheduleAtFixedRate(() -> {
//            var result = redisTemplate.convertAndSend("namespace-change", "test");
//            System.out.println("Send with value " + result);
//        }, 1, 3L, TimeUnit.SECONDS);
//    }

    private final RedisTemplate<String, Object> redisTemplate;

    public NamespaceVersionChangeListener(RedisMessageListenerContainer redisConnectionFactory,
                         RedisTemplate<String, Object> redisTemplate) {
        ChannelTopic channelTopic = new ChannelTopic("namespace-change");
        redisConnectionFactory.addMessageListener(this, channelTopic);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        System.out.println("Received Message: " + message);
        System.out.println("Received Message: " + new String(message.getBody()));
    }
}
