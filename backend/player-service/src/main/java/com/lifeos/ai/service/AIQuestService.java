package com.lifeos.ai.service;

import com.lifeos.quest.dto.QuestRequest;
import java.util.List;
import java.util.UUID;

public interface AIQuestService {
    List<QuestRequest> generateQuests(UUID playerId, int count);
    QuestRequest generatePenaltyQuest(UUID playerId, UUID failedQuestId);
    QuestRequest generatePromotionExam(UUID playerId, com.lifeos.player.domain.enums.PlayerRank fromRank, com.lifeos.player.domain.enums.PlayerRank toRank);
    
    // Future expansion:
    // String analyzePerformance(UUID playerId);
}
