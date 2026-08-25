package com.fashionpin.fashiondiscoveryservice.controller;

import com.fashionpin.fashiondiscoveryservice.dto.DiscoveryFilter;
import com.fashionpin.fashiondiscoveryservice.dto.FashionPostResponse;
import com.fashionpin.fashiondiscoveryservice.entity.Occasion;
import com.fashionpin.fashiondiscoveryservice.entity.Style;
import com.fashionpin.fashiondiscoveryservice.service.DiscoveryFeedService;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fashion")
@RequiredArgsConstructor
public class DiscoveryController {

    private final DiscoveryFeedService feedService;

    @GetMapping("/discover")
    public ResponseEntity<Page<FashionPostResponse>> discover(
            @RequestParam(value = "style", required = false) Style style,
            @RequestParam(value = "occasion", required = false) Occasion occasion,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "color", required = false) String color,
            @RequestParam(value = "season", required = false) String season,
            @PageableDefault(size = 20) Pageable pageable) {

        DiscoveryFilter filter = DiscoveryFilter.builder()
                .style(style)
                .occasion(occasion)
                .category(category)
                .color(color)
                .season(season)
                .build();

        return ResponseEntity.ok(feedService.discover(filter, pageable));
    }

    @GetMapping("/styles")
    public ResponseEntity<List<String>> getStyles() {
        List<String> styles = Arrays.stream(Style.values()).map(Enum::name).toList();
        return ResponseEntity.ok(styles);
    }

    @GetMapping("/occasions")
    public ResponseEntity<List<String>> getOccasions() {
        List<String> occasions = Arrays.stream(Occasion.values()).map(Enum::name).toList();
        return ResponseEntity.ok(occasions);
    }
}
