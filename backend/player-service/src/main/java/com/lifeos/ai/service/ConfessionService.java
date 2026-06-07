package com.lifeos.ai.service;

import java.util.UUID;

public interface ConfessionService {
    ConfessionResult judgeConfession(UUID playerId, String confessionText);
}
