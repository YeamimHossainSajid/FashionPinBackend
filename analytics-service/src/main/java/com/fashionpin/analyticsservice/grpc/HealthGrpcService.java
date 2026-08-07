package com.fashionpin.analyticsservice.grpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HealthGrpcService {

    private static final Logger log = LoggerFactory.getLogger(HealthGrpcService.class);

    public String check() {
        // Placeholder until protobuf stubs are generated in CI/local builds.
        log.debug("gRPC health placeholder invoked");
        return "UP";
    }
}

