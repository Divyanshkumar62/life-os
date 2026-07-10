package com.lifeos.core.controller;

import com.lifeos.core.dto.JobChangeRequest;
import com.lifeos.core.dto.JobChangeResponse;
import com.lifeos.progression.service.JobChangeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("coreJobChangeController")
@RequestMapping("/api/player/job-change")
@RequiredArgsConstructor
public class JobChangeController {

    private final JobChangeService jobChangeService;

    @PostMapping("/select-class")
    public ResponseEntity<JobChangeResponse> selectClass(@Valid @RequestBody JobChangeRequest request) {
        JobChangeResponse response = jobChangeService.selectJobClass(request.getPlayerId(), request.getSelectedClass());
        return ResponseEntity.ok(response);
    }
}
