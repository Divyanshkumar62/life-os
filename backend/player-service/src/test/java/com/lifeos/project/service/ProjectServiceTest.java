package com.lifeos.project.service;

import com.lifeos.player.domain.PlayerIdentity;
import com.lifeos.player.domain.PlayerProgression;
import com.lifeos.player.domain.enums.PlayerRank;
import com.lifeos.player.repository.PlayerProgressionRepository;
import com.lifeos.progression.domain.UserBossKey;
import com.lifeos.progression.repository.UserBossKeyRepository;
import com.lifeos.project.domain.Project;
import com.lifeos.project.domain.enums.ProjectStatus;
import com.lifeos.project.repository.ProjectRepository;
import com.lifeos.quest.repository.QuestRepository;
import com.lifeos.quest.domain.enums.QuestState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private QuestRepository questRepository;
    @Mock private PlayerProgressionRepository progressionRepository;
    @Mock private UserBossKeyRepository bossKeyRepository;

    @InjectMocks
    private ProjectService projectService;

    private UUID playerId;
    private PlayerIdentity player;
    private PlayerProgression progression;
    private Project validProject;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        player = PlayerIdentity.builder().playerId(playerId).build();
        
        progression = PlayerProgression.builder()
                .player(player)
                .rank(PlayerRank.E) // E-Rank: 1 Slot
                .build();
                
        validProject = Project.builder()
                .player(player)
                .rankRequirement(PlayerRank.E)
                .minSubtasks(5)
                .durationDays(7)
                .startDate(LocalDateTime.now())
                .bossKeyReward(1)
                .status(ProjectStatus.ACTIVE) // Must set explicitly as builder bypasses @PrePersist
                .build();
    }

    @Test
    void testCreateProject_Success() {
        when(progressionRepository.findByPlayerPlayerId(playerId)).thenReturn(Optional.of(progression));
        // Active projects: 0
        when(projectRepository.countByPlayerPlayerIdAndStatus(playerId, ProjectStatus.ACTIVE)).thenReturn(0L);
        when(projectRepository.save(any(Project.class))).thenAnswer(i -> i.getArgument(0));
        
        Project created = projectService.createProject(validProject);
        
        assertNotNull(created.getHardDeadline()); // Should be set automatically
        assertEquals(ProjectStatus.ACTIVE, created.getStatus());
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void testCreateProject_Fail_ShortDuration() {
        validProject.setDurationDays(3);
        
        assertThrows(IllegalArgumentException.class, () -> projectService.createProject(validProject));
    }

    @Test
    void testCreateProject_Fail_SlotLimitReached() {
        when(progressionRepository.findByPlayerPlayerId(playerId)).thenReturn(Optional.of(progression));
        // Active projects: 1 (Max for E-Rank)
        when(projectRepository.countByPlayerPlayerIdAndStatus(playerId, ProjectStatus.ACTIVE)).thenReturn(1L);
        
        assertThrows(IllegalStateException.class, () -> projectService.createProject(validProject));
    }
    
    @Test
    void testCompleteProject_Success_AwardsKey() {
        UUID projectId = UUID.randomUUID();
        validProject.setProjectId(projectId);
        validProject.setStatus(ProjectStatus.ACTIVE);
        // Past start date to satisfy duration (7 days)
        validProject.setStartDate(LocalDateTime.now().minusDays(8));
        validProject.setHardDeadline(LocalDateTime.now().plusDays(1));

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(validProject));
        when(questRepository.countByProjectIdAndState(projectId, QuestState.COMPLETED)).thenReturn(5L); // 5 subtasks completed
        when(progressionRepository.findByPlayerPlayerId(playerId)).thenReturn(Optional.of(progression));
        when(bossKeyRepository.findByPlayerPlayerIdAndRank(playerId, PlayerRank.E)).thenReturn(Optional.empty()); // New key record
        
        projectService.completeProject(projectId);
        
        assertEquals(ProjectStatus.COMPLETED, validProject.getStatus());
        verify(bossKeyRepository).save(argThat(key -> key.getKeyCount() == 1 && key.getRank() == PlayerRank.E));
    }
}
