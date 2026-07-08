package com.lifeos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeos.player.dto.PlayerStateResponse;
import com.lifeos.player.service.PlayerStateService;
import com.lifeos.project.dto.DungeonOpenRequest;
import com.lifeos.project.dto.DungeonResponse;
import com.lifeos.project.domain.Project;
import com.lifeos.project.service.DungeonArchitectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-mysql")
@Transactional
public class DungeonIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PlayerStateService playerStateService;

    @MockBean
    private DungeonArchitectService dungeonArchitectService;

    private UUID playerId;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize player
        PlayerStateResponse response = playerStateService.initializePlayer("DungeonTester");
        playerId = response.getIdentity().getPlayerId();

        // Stub DungeonArchitectService
        String dungeonResponseJson = """
            {
              "is_valid": true,
              "rejection_reason": null,
              "dungeon": {
                "title": "C-Rank Dungeon: Shadow Labyrinth",
                "description": "Clear the shadow labyrinth floor by floor.",
                "rank": "C",
                "boss_name": "Labyrinth Guardian",
                "stat_focus": "INT",
                "estimated_duration_days": 10,
                "floors": [
                  { "floor_num": 1, "title": "Entrance", "xp": 100, "stat": "INT" },
                  { "floor_num": 2, "title": "Shadow Hall", "xp": 150, "stat": "INT" },
                  { "floor_num": 3, "title": "Core Chamber", "xp": 200, "stat": "INT" }
                ],
                "loot": {
                  "xp_total": 1000,
                  "gold": 500,
                  "boss_keys": 1
                }
              }
            }
            """;
        DungeonResponse mockResponse = objectMapper.readValue(dungeonResponseJson, DungeonResponse.class);
        when(dungeonArchitectService.generateDungeon(any(UUID.class), anyString(), anyString())).thenReturn(mockResponse);
    }

    @Test
    void testDungeonOpeningLoop() throws Exception {
        // Step A (The Cheat): Grant KEY_C_RANK item to inventory
        mockMvc.perform(post("/api/inventory/" + playerId + "/grant")
                .param("itemCode", "KEY_C_RANK")
                .param("quantity", "1"))
                .andExpect(status().isOk());

        // Step B (The Lockout Test): Try to open with fake key (KEY_S_RANK)
        DungeonOpenRequest lockoutRequest = new DungeonOpenRequest(playerId, "KEY_S_RANK");
        MvcResult lockoutResult = mockMvc.perform(post("/api/dungeons/open")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(lockoutRequest)))
                .andReturn();

        int status = lockoutResult.getResponse().getStatus();
        assertTrue(status == 403 || status == 500, "Expected status 403 or 500 but got: " + status);

        String responseBody = lockoutResult.getResponse().getContentAsString();
        assertTrue(responseBody.contains("Gate access denied"),
                "Expected error message to contain 'Gate access denied' but got: " + responseBody);

        // Step C (The Opening): Open with KEY_C_RANK
        DungeonOpenRequest openRequest = new DungeonOpenRequest(playerId, "KEY_C_RANK");
        MvcResult openResult = mockMvc.perform(post("/api/dungeons/open")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(openRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String projectResponseJson = openResult.getResponse().getContentAsString();
        Project project = objectMapper.readValue(projectResponseJson, Project.class);
        assertNotNull(project);
        assertNotNull(project.getProjectId());

        // Step D (Dynamic Quests): Perform a GET to retrieve pending subtasks
        mockMvc.perform(get("/api/players/" + playerId + "/quests")
                .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quests").isArray())
                .andExpect(jsonPath("$.quests[0].title").value("Floor 1: Entrance"))
                .andExpect(jsonPath("$.quests[1].title").value("Floor 2: Shadow Hall"))
                .andExpect(jsonPath("$.quests[2].title").value("Floor 3: Core Chamber"));
    }
}
