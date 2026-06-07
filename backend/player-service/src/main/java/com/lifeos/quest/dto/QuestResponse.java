package com.lifeos.quest.dto;

import com.lifeos.quest.domain.Quest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO wrapper for Quest responses including system notifications.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestResponse {
    private Quest quest;
    private List<Quest> quests = new ArrayList<>();
    private List<String> systemMessages = new ArrayList<>();
}
