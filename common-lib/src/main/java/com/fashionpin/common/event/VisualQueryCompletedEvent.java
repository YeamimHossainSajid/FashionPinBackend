package com.fashionpin.common.event;

import com.fashionpin.common.dto.VisualMatchDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisualQueryCompletedEvent {
    private UUID jobId;
    private String status;
    private List<VisualMatchDto> matches;
    private String errorDetails;
    private Instant completedAt;
}
