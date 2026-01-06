package com.lifeos.player.dto;

import com.lifeos.player.domain.enums.StatusFlagType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerStatusFlagDTO {
    private StatusFlagType flag;
    private LocalDateTime acquiredAt;
    private LocalDateTime expiresAt;
}
