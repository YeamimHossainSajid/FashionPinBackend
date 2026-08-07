package com.fashionpin.imageprocessingservice.mapper;

import com.fashionpin.imageprocessingservice.dto.HealthResponse;
import java.util.Map;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HealthMapper {

    default HealthResponse toResponse(Map<String, String> source) {
        return HealthResponse.builder()
                .status(source.get("status"))
                .service(source.get("service"))
                .build();
    }
}

