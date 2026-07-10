package com.lifeos.core.controller;

import com.lifeos.core.dto.SurvivalTaskSubmitRequest;
import com.lifeos.core.dto.SurvivalTaskSubmitResponse;
import com.lifeos.core.dto.TaskRerollRequest;
import com.lifeos.core.dto.TaskRerollResponse;
import com.lifeos.core.service.PenaltyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("corePenaltyController")
@RequestMapping("/api/penalty")
@RequiredArgsConstructor
public class PenaltyController {

    private final PenaltyService penaltyService;

    @PostMapping("/submit-task")
    public ResponseEntity<SurvivalTaskSubmitResponse> submitTask(@Valid @RequestBody SurvivalTaskSubmitRequest request) {
        SurvivalTaskSubmitResponse response = penaltyService.submitSurvivalTask(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reroll")
    public ResponseEntity<TaskRerollResponse> reroll(@Valid @RequestBody TaskRerollRequest request) {
        TaskRerollResponse response = penaltyService.rerollTask(request.getPlayerId(), request.getReason());
        return ResponseEntity.ok(response);
    }
}
