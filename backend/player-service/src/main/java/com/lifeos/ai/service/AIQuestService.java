package com.lifeos.ai.service;

import com.lifeos.quest.dto.QuestRequest;
import java.util.List;
import java.util.UUID;

public interface AIQuestService {
    List<QuestRequest> generateQuests(UUID playerId, int count);
    
    // Future expansion:
    // String analyzePerformance(UUID playerId);
}
