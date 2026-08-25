package com.fashionpin.outfitdetectionservice.controller;

import com.fashionpin.outfitdetectionservice.dto.CreateDetectionJobRequest;
import com.fashionpin.outfitdetectionservice.dto.DetectionJobResponse;
import com.fashionpin.outfitdetectionservice.dto.DetectionResultResponse;
import com.fashionpin.outfitdetectionservice.service.OutfitDetectionJobService;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/outfit-detection/jobs")
@RequiredArgsConstructor
public class OutfitDetectionJobController {

    private final OutfitDetectionJobService jobService;

    @PostMapping
    public ResponseEntity<DetectionJobResponse> createJob(
            Principal principal,
            @Valid @RequestBody CreateDetectionJobRequest request) {
        String userId = principal.getName();
        DetectionJobResponse response = jobService.createJob(userId, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetectionJobResponse> getJobById(
            @PathVariable("id") String id,
            Principal principal) {
        String userId = principal.getName();
        return ResponseEntity.ok(jobService.getJobById(id, userId));
    }

    @GetMapping("/{id}/result")
    public ResponseEntity<DetectionResultResponse> getJobResult(
            @PathVariable("id") String id,
            Principal principal) {
        String userId = principal.getName();
        return ResponseEntity.ok(jobService.getJobResult(id, userId));
    }
}
