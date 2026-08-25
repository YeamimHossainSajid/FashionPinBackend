package com.fashionpin.fashiondiscoveryservice.controller;

import com.fashionpin.fashiondiscoveryservice.dto.CreateOutfitRequest;
import com.fashionpin.fashiondiscoveryservice.dto.OutfitResponse;
import com.fashionpin.fashiondiscoveryservice.dto.UpdateOutfitRequest;
import com.fashionpin.fashiondiscoveryservice.service.OutfitService;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/fashion/outfits")
@RequiredArgsConstructor
public class OutfitController {

    private final OutfitService outfitService;

    @PostMapping
    public ResponseEntity<OutfitResponse> createOutfit(
            Principal principal,
            @Valid @RequestBody CreateOutfitRequest request) {
        String userId = principal.getName();
        OutfitResponse response = outfitService.createOutfit(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OutfitResponse> getOutfitById(@PathVariable("id") String id) {
        return ResponseEntity.ok(outfitService.getOutfitById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<OutfitResponse> updateOutfit(
            @PathVariable("id") String id,
            Principal principal,
            @Valid @RequestBody UpdateOutfitRequest request) {
        String userId = principal.getName();
        return ResponseEntity.ok(outfitService.updateOutfit(id, userId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOutfit(
            @PathVariable("id") String id,
            Principal principal) {
        String userId = principal.getName();
        outfitService.deleteOutfit(id, userId);
        return ResponseEntity.noContent().build();
    }
}
