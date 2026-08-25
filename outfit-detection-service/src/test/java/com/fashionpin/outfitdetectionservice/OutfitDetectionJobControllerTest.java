package com.fashionpin.outfitdetectionservice;

import com.fashionpin.outfitdetectionservice.dto.CreateDetectionJobRequest;
import com.fashionpin.outfitdetectionservice.dto.DetectionJobResponse;
import com.fashionpin.outfitdetectionservice.dto.DetectionResultResponse;
import com.fashionpin.outfitdetectionservice.entity.DetectionJobStatus;
import com.fashionpin.outfitdetectionservice.service.OutfitDetectionJobService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OutfitDetectionJobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OutfitDetectionJobService jobService;

    @Test
    @WithMockUser(username = "user-123")
    void shouldCreateDetectionJob() throws Exception {
        DetectionJobResponse response = DetectionJobResponse.builder()
                .id("job-1")
                .userId("user-123")
                .inputMediaId("media-1")
                .status(DetectionJobStatus.QUEUED)
                .build();

        given(jobService.createJob(eq("user-123"), any(CreateDetectionJobRequest.class))).willReturn(response);

        String payload = """
            {
                "inputMediaId": "media-1"
            }
            """;

        mockMvc.perform(post("/api/outfit-detection/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value("job-1"))
                .andExpect(jsonPath("$.status").value("QUEUED"));
    }

    @Test
    @WithMockUser(username = "user-123")
    void shouldGetJobById() throws Exception {
        DetectionJobResponse response = DetectionJobResponse.builder()
                .id("job-1")
                .userId("user-123")
                .inputMediaId("media-1")
                .status(DetectionJobStatus.QUEUED)
                .build();

        given(jobService.getJobById("job-1", "user-123")).willReturn(response);

        mockMvc.perform(get("/api/outfit-detection/jobs/job-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("job-1"));
    }

    @Test
    @WithMockUser(username = "user-123")
    void shouldGetJobResult() throws Exception {
        DetectionResultResponse response = DetectionResultResponse.builder()
                .id("result-1")
                .jobId("job-1")
                .detectedItems(List.of())
                .build();

        given(jobService.getJobResult("job-1", "user-123")).willReturn(response);

        mockMvc.perform(get("/api/outfit-detection/jobs/job-1/result"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value("job-1"));
    }
}
