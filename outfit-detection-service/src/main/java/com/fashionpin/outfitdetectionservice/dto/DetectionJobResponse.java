package com.fashionpin.outfitdetectionservice.dto;

import com.fashionpin.outfitdetectionservice.entity.DetectionJobStatus;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetectionJobResponse {
    private String id;
    private String userId;
    private String inputMediaId;
    private DetectionJobStatus status;
    private String errorMessage;
    private DetectionResultResponse result;
    private Instant createdAt;
    private Instant updatedAt;
}
