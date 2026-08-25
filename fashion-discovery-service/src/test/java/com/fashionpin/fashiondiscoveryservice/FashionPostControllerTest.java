package com.fashionpin.fashiondiscoveryservice;

import com.fashionpin.fashiondiscoveryservice.dto.CreateFashionPostRequest;
import com.fashionpin.fashiondiscoveryservice.dto.FashionPostResponse;
import com.fashionpin.fashiondiscoveryservice.service.FashionPostService;
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
class FashionPostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FashionPostService postService;

    @Test
    @WithMockUser(username = "user-123")
    void shouldCreateFashionPost() throws Exception {
        FashionPostResponse response = FashionPostResponse.builder()
                .id("post-1")
                .authorUserId("user-123")
                .caption("Summer Streetwear")
                .mediaIds(List.of("media-1"))
                .build();

        given(postService.createPost(eq("user-123"), any(CreateFashionPostRequest.class))).willReturn(response);

        String payload = """
            {
                "caption": "Summer Streetwear",
                "mediaIds": ["media-1"]
            }
            """;

        mockMvc.perform(post("/api/fashion/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("post-1"))
                .andExpect(jsonPath("$.caption").value("Summer Streetwear"));
    }

    @Test
    void shouldGetPostById() throws Exception {
        FashionPostResponse response = FashionPostResponse.builder()
                .id("post-1")
                .authorUserId("user-123")
                .caption("Summer Streetwear")
                .mediaIds(List.of("media-1"))
                .build();

        given(postService.getPostById("post-1")).willReturn(response);

        mockMvc.perform(get("/api/fashion/posts/post-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("post-1"));
    }
}
