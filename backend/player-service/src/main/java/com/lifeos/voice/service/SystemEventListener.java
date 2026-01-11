package com.lifeos.voice.service;

import com.lifeos.voice.event.VoiceSystemEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SystemEventListener {

    private static final Logger log = LoggerFactory.getLogger(SystemEventListener.class);
    private final SystemVoiceService voiceService;

    @Async // Decouple from main transaction where possible
    @EventListener
    public void handleVoiceSystemEvent(VoiceSystemEvent event) {
        try {
            voiceService.generateMessage(
                event.getPlayerId(), 
                event.getType(), 
                event.getPayload()
            );
        } catch (Exception e) {
            log.error("Failed to process System Voice Event for player {}", event.getPlayerId(), e);
        }
    }
}
