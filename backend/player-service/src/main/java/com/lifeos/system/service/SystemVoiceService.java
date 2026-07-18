package com.lifeos.system.service;

import com.lifeos.system.domain.SystemEvent;
import com.lifeos.system.domain.enums.SystemEventType;
import com.lifeos.system.repository.SystemEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service("systemVoiceService")
public class SystemVoiceService {

    private static final Logger log = LoggerFactory.getLogger(SystemVoiceService.class);

    private final SystemEventRepository systemEventRepository;

    public SystemVoiceService(SystemEventRepository systemEventRepository) {
        this.systemEventRepository = systemEventRepository;
    }

    @Transactional
    public void emitEvent(UUID playerId, SystemEventType eventType, String message) {
        emitEvent(playerId, eventType, message, null);
    }

    @Transactional
    public void emitEvent(UUID playerId, SystemEventType eventType, String message, String payloadJson) {
        log.info("System Voice Output for {}: [{}] {} (payload={})", playerId, eventType, message, payloadJson);
        
        SystemEvent event = new SystemEvent();
        event.setPlayerId(playerId);
        event.setEventType(eventType);
        event.setMessage(message);
        event.setPayloadJson(payloadJson);
        event.setConsumed(false);
        event.setCreatedAt(LocalDateTime.now());
        
        systemEventRepository.save(event);
    }
    
    @Transactional
    public List<SystemEvent> getPlayerEvents(UUID playerId) {
        List<SystemEvent> events = systemEventRepository.findByPlayerIdAndIsConsumedFalseOrderByCreatedAtAsc(playerId);
        if (!events.isEmpty()) {
            for (SystemEvent event : events) {
                event.setConsumed(true);
            }
            systemEventRepository.saveAll(events);
        }
        return events;
    }
}
