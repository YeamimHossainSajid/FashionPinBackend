package com.fashionpin.fashiondiscoveryservice.controller;

import com.fashionpin.fashiondiscoveryservice.dto.CreateFashionPostRequest;
import com.fashionpin.fashiondiscoveryservice.dto.FashionPostResponse;
import com.fashionpin.fashiondiscoveryservice.dto.UpdateFashionPostRequest;
import com.fashionpin.fashiondiscoveryservice.service.FashionPostService;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fashion/posts")
@RequiredArgsConstructor
public class FashionPostController {

    private final FashionPostService postService;

    @PostMapping
    public ResponseEntity<FashionPostResponse> createPost(
            Principal principal,
            @Valid @RequestBody CreateFashionPostRequest request) {
        String userId = principal.getName();
        FashionPostResponse response = postService.createPost(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FashionPostResponse> getPostById(@PathVariable("id") String id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @GetMapping
    public ResponseEntity<Page<FashionPostResponse>> getPosts(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(postService.getPosts(pageable));
    }

    @GetMapping("/by-product/{productId}")
    public ResponseEntity<Page<FashionPostResponse>> getPostsByProductId(
            @PathVariable("productId") String productId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(postService.getPostsByProductId(productId, pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FashionPostResponse> updatePost(
            @PathVariable("id") String id,
            Principal principal,
            @Valid @RequestBody UpdateFashionPostRequest request) {
        String userId = principal.getName();
        return ResponseEntity.ok(postService.updatePost(id, userId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable("id") String id,
            Principal principal) {
        String userId = principal.getName();
        postService.deletePost(id, userId);
        return ResponseEntity.noContent().build();
    }
}
