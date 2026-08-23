package com.fashionpin.brandintegrationservice.service;

import com.fashionpin.brandintegrationservice.dto.BrandResponse;
import com.fashionpin.brandintegrationservice.dto.CreateBrandRequest;
import com.fashionpin.brandintegrationservice.dto.UpdateBrandRequest;
import com.fashionpin.brandintegrationservice.entity.Brand;
import com.fashionpin.brandintegrationservice.repository.BrandRepository;
import com.fashionpin.common.event.BrandCreatedEvent;
import com.fashionpin.common.event.BrandDeletedEvent;
import com.fashionpin.common.event.BrandUpdatedEvent;
import com.fashionpin.common.exception.BusinessException;
import com.fashionpin.common.exception.ResourceNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BrandService {

    private final BrandRepository brandRepository;
    private final OutboxService outboxService;

    public BrandService(BrandRepository brandRepository, OutboxService outboxService) {
        this.brandRepository = brandRepository;
        this.outboxService = outboxService;
    }

    @Transactional
    public BrandResponse createBrand(CreateBrandRequest request) {
        String formattedSlug = request.getSlug().toLowerCase().trim().replaceAll("[^a-z0-9-]", "-");
        if (brandRepository.existsBySlug(formattedSlug)) {
            throw new BusinessException("BRAND_SLUG_ALREADY_EXISTS", "Brand slug already exists: " + formattedSlug);
        }

        Brand brand = Brand.create(
                request.getName(),
                formattedSlug,
                request.getDescription(),
                request.getLogoMediaId(),
                request.getWebsite(),
                request.getStatus()
        );

        Brand saved = brandRepository.save(brand);

        BrandCreatedEvent createdEvent = BrandCreatedEvent.create(saved.getId(), saved.getName(), saved.getSlug(), null);
        outboxService.saveEvent("Brand", saved.getId(), "BrandCreated", createdEvent);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public BrandResponse getBrandById(String id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));
        return mapToResponse(brand);
    }

    @Transactional(readOnly = true)
    public List<BrandResponse> getAllBrands() {
        return brandRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BrandResponse updateBrand(String id, UpdateBrandRequest request) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));

        if (request.getName() != null) {
            brand.setName(request.getName());
        }
        if (request.getDescription() != null) {
            brand.setDescription(request.getDescription());
        }
        if (request.getLogoMediaId() != null) {
            brand.setLogoMediaId(request.getLogoMediaId());
        }
        if (request.getWebsite() != null) {
            brand.setWebsite(request.getWebsite());
        }
        if (request.getStatus() != null) {
            brand.setStatus(request.getStatus());
        }

        brand.setUpdatedAt(Instant.now());
        Brand updated = brandRepository.save(brand);

        BrandUpdatedEvent updatedEvent = BrandUpdatedEvent.create(updated.getId(), updated.getName(), updated.getSlug(), null);
        outboxService.saveEvent("Brand", updated.getId(), "BrandUpdated", updatedEvent);

        return mapToResponse(updated);
    }

    @Transactional
    public void deleteBrand(String id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));

        brandRepository.delete(brand);

        BrandDeletedEvent deletedEvent = BrandDeletedEvent.create(id, null);
        outboxService.saveEvent("Brand", id, "BrandDeleted", deletedEvent);
    }

    private BrandResponse mapToResponse(Brand brand) {
        return BrandResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .slug(brand.getSlug())
                .description(brand.getDescription())
                .logoMediaId(brand.getLogoMediaId())
                .website(brand.getWebsite())
                .status(brand.getStatus())
                .createdAt(brand.getCreatedAt())
                .updatedAt(brand.getUpdatedAt())
                .build();
    }
}
