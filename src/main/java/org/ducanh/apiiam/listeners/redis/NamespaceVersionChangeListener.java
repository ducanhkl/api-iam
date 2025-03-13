package org.ducanh.apiiam.listeners.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.ducanh.apiiam.dto.events.NamespaceChangeEvent;
import org.ducanh.apiiam.storage.PolicyStorageManagement;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import static org.ducanh.apiiam.Constants.NAMESPACE_CHANGE_TOPIC;

@Component
@Slf4j
public class NamespaceVersionChangeListener implements MessageListener {

    private final ObjectMapper objectMapper;
    private final PolicyStorageManagement policyStorageManagement;

    public NamespaceVersionChangeListener(RedisMessageListenerContainer redisConnectionFactory,
                                          ObjectMapper objectMapper,
                                          PolicyStorageManagement policyStorageManagement) {
        ChannelTopic channelTopic = new ChannelTopic(NAMESPACE_CHANGE_TOPIC);
        redisConnectionFactory.addMessageListener(this, channelTopic);
        this.objectMapper = objectMapper;
        this.policyStorageManagement = policyStorageManagement;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        var rawBody = new String(message.getBody());
        log.info("Received namespace version change rawMessage: {}", rawBody);
        try {
            NamespaceChangeEvent event = objectMapper.readValue(rawBody, NamespaceChangeEvent.class);
            log.info("Received namespace version change event: {}", event);
            policyStorageManagement.reloadPolicy(event.namespaceId());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
