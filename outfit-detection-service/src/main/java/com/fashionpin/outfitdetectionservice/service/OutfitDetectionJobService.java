package com.fashionpin.outfitdetectionservice.service;

import com.fashionpin.common.event.OutfitDetectionRequestedEvent;
import com.fashionpin.common.exception.ResourceNotFoundException;
import com.fashionpin.outfitdetectionservice.dto.CreateDetectionJobRequest;
import com.fashionpin.outfitdetectionservice.dto.DetectedItemDto;
import com.fashionpin.outfitdetectionservice.dto.DetectionJobResponse;
import com.fashionpin.outfitdetectionservice.dto.DetectionResultResponse;
import com.fashionpin.outfitdetectionservice.engine.OutfitDetectionEngine;
import com.fashionpin.outfitdetectionservice.entity.DetectionJob;

import com.fashionpin.outfitdetectionservice.kafka.OutfitDetectionEventProducer;
import com.fashionpin.outfitdetectionservice.repository.DetectionJobRepository;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutfitDetectionJobService {

    private final DetectionJobRepository jobRepository;
    private final OutfitDetectionEngine detectionEngine;
    private final OutfitDetectionEventProducer eventProducer;

    @Transactional
    public DetectionJobResponse createJob(String userId, CreateDetectionJobRequest request) {
        log.info("Creating outfit detection job for userId: {}, mediaId: {}", userId, request.getInputMediaId());

        DetectionJob job = DetectionJob.builder()
                .userId(userId)
                .inputMediaId(request.getInputMediaId())
                .build();

        DetectionJob saved = jobRepository.save(job);
        DetectionJob processed = detectionEngine.process(saved);
        DetectionJob finalJob = jobRepository.save(processed);

        eventProducer.publishJobRequested(OutfitDetectionRequestedEvent.create(
                finalJob.getId(), userId, finalJob.getInputMediaId(), null));

        return mapToResponse(finalJob);
    }

    @Transactional(readOnly = true)
    public DetectionJobResponse getJobById(String id, String userId) {
        DetectionJob job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Detection job not found with id: " + id));

        if (!job.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized to access this detection job");
        }

        return mapToResponse(job);
    }

    @Transactional(readOnly = true)
    public DetectionResultResponse getJobResult(String id, String userId) {
        DetectionJob job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Detection job not found with id: " + id));

        if (!job.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized to access this detection job result");
        }

        if (job.getResult() == null) {
            return DetectionResultResponse.builder()
                    .id(null)
                    .jobId(job.getId())
                    .detectedItems(List.of())
                    .createdAt(null)
                    .build();
        }

        List<DetectedItemDto> itemDtos = job.getResult().getDetectedItems().stream()
                .map(item -> DetectedItemDto.builder()
                        .id(item.getId())
                        .category(item.getCategory())
                        .confidence(item.getConfidence())
                        .build())
                .toList();

        return DetectionResultResponse.builder()
                .id(job.getResult().getId())
                .jobId(job.getId())
                .detectedItems(itemDtos)
                .createdAt(job.getResult().getCreatedAt())
                .build();
    }

    public DetectionJobResponse mapToResponse(DetectionJob job) {
        DetectionResultResponse resultResp = null;
        if (job.getResult() != null) {
            List<DetectedItemDto> items = job.getResult().getDetectedItems().stream()
                    .map(item -> DetectedItemDto.builder()
                            .id(item.getId())
                            .category(item.getCategory())
                            .confidence(item.getConfidence())
                            .build())
                    .toList();

            resultResp = DetectionResultResponse.builder()
                    .id(job.getResult().getId())
                    .jobId(job.getId())
                    .detectedItems(items)
                    .createdAt(job.getResult().getCreatedAt())
                    .build();
        }

        return DetectionJobResponse.builder()
                .id(job.getId())
                .userId(job.getUserId())
                .inputMediaId(job.getInputMediaId())
                .status(job.getStatus())
                .errorMessage(job.getErrorMessage())
                .result(resultResp)
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }
}
