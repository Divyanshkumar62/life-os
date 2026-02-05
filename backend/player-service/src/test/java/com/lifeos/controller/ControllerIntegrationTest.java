package com.lifeos.controller;

import com.lifeos.player.domain.enums.AttributeType;
import com.lifeos.player.dto.PlayerStateResponse;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.quest.dto.QuestRequest;
import com.lifeos.quest.service.QuestLifecycleService;
import com.lifeos.progression.service.ProgressionService;
import com.lifeos.penalty.service.PenaltyService;
import com.lifeos.player.repository.PlayerIdentityRepository;
import com.lifeos.progression.repository.UserBossKeyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QuestLifecycleService questService;

    @MockBean
    private ProgressionService progressionService;

    @MockBean
    private PlayerStateService playerStateService;

    @MockBean
    private PenaltyService penaltyService;

    @MockBean
    private PlayerIdentityRepository playerIdentityRepository;

    @MockBean
    private UserBossKeyRepository bossKeyRepository;

    @Test
    void testQuestAssignment() throws Exception {
        QuestRequest request = new QuestRequest();
        request.setTitle("Test Quest");

        mockMvc.perform(post("/api/quests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(questService).assignQuest(any());
    }

    @Test
    void testQuestCompletion() throws Exception {
        UUID questId = UUID.randomUUID();
        mockMvc.perform(post("/api/quests/" + questId + "/complete"))
                .andExpect(status().isOk());

        verify(questService).completeQuest(questId);
    }

    @Test
    void testPromotionRequest() throws Exception {
        UUID playerId = UUID.randomUUID();
        mockMvc.perform(post("/api/progression/" + playerId + "/request-promotion"))
                .andExpect(status().isOk());

        verify(progressionService).requestPromotion(playerId);
    }

    @Test
    void testAdminAddXp() throws Exception {
        UUID playerId = UUID.randomUUID();
        mockMvc.perform(post("/api/admin/players/" + playerId + "/add-xp")
                .param("amount", "100"))
                .andExpect(status().isOk());

        verify(playerStateService).addXp(playerId, 100);
    }

    @Test
    void testAdminPenaltyEnter() throws Exception {
        UUID playerId = UUID.randomUUID();
        mockMvc.perform(post("/api/admin/players/" + playerId + "/penalty/enter")
                .param("reason", "Manual trigger"))
                .andExpect(status().isOk());

        verify(penaltyService).enterPenaltyZone(playerId, "Manual trigger");
    }
}
