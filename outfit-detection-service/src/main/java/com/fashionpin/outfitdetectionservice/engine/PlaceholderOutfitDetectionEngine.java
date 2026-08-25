package com.fashionpin.outfitdetectionservice.engine;

import com.fashionpin.outfitdetectionservice.entity.DetectionJob;
import com.fashionpin.outfitdetectionservice.entity.DetectionJobStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PlaceholderOutfitDetectionEngine implements OutfitDetectionEngine {

    @Override
    public DetectionJob process(DetectionJob job) {
        log.info("PlaceholderOutfitDetectionEngine processing detection job: {}", job.getId());
        // Clean AI model abstraction: transition to QUEUED state for async worker processing
        job.setStatus(DetectionJobStatus.QUEUED);
        return job;
    }
}
