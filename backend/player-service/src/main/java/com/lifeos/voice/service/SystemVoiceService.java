package com.lifeos.voice.service;

import com.lifeos.voice.domain.SystemMessage;
import com.lifeos.voice.domain.enums.SystemMessageType;
import com.lifeos.voice.domain.enums.SystemVoiceMode;
import com.lifeos.voice.repository.SystemMessageRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SystemVoiceService {

    private static final Logger log = LoggerFactory.getLogger(SystemVoiceService.class);
    private final SystemMessageRepository messageRepository;

    /**
     * Generates and persists a system message based on type and payload.
     * @param playerId Target player
     * @param type Message Type (defines Template)
     * @param payload Dynamic values for template replacement
     */
    @Transactional
    public void generateMessage(UUID playerId, SystemMessageType type, Map<String, Object> payload) {
        String template = type.getTemplate();
        String resolvedBody = resolveTemplate(template, payload);
        
        SystemMessage message = SystemMessage.builder()
                .playerId(playerId)
                .type(type)
                .mode(type.getDefaultMode()) // Can be overridden if needed
                .title(getTitleForMode(type.getDefaultMode()))
                .body(resolvedBody)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        
        messageRepository.save(message);
        log.info("System Voice: [{}] {}", type.getDefaultMode(), resolvedBody.replace("\n", " "));
    }

    private String resolveTemplate(String template, Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            return template;
        }
        String result = template;
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            String key = "${" + entry.getKey() + "}";
            String value = String.valueOf(entry.getValue());
            result = result.replace(key, value);
        }
        return result;
    }

    private String getTitleForMode(SystemVoiceMode mode) {
        return switch (mode) {
            case REWARD -> "SYSTEM REWARD";
            case WARNING -> "SYSTEM WARNING";
            case PENALTY -> "SYSTEM PENALTY";
            case PROMOTION -> "SYSTEM NOTICE"; // Or SUCCESS?
            case FAILURE -> "SYSTEM FAILURE";
            default -> "SYSTEM MESSAGE";
        };
    }
}
