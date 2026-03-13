package com.lifeos.quest.service;

import com.lifeos.quest.domain.Quest;
import com.lifeos.quest.dto.QuestRequest;
import com.lifeos.quest.repository.QuestRepository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public interface QuestLifecycleService {
    Quest assignQuest(QuestRequest request);
    
    void updateQuest(UUID questId, Map<String, Object> updates, String reason);
    
    void completeQuest(UUID questId);
    
    void failQuest(UUID questId);
    
    void expireQuest(UUID questId);
    
    QuestRepository getQuestRepository();
}
