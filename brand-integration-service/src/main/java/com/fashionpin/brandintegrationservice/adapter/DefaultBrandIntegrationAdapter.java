package com.fashionpin.brandintegrationservice.adapter;

import com.fashionpin.brandintegrationservice.port.BrandIntegrationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DefaultBrandIntegrationAdapter implements BrandIntegrationPort {

    private static final Logger log = LoggerFactory.getLogger(DefaultBrandIntegrationAdapter.class);

    @Override
    public boolean syncBrandCatalog(String brandId, String providerName) {
        log.info("Placeholder brand catalog sync invoked for brandId={} provider={}", brandId, providerName);
        return true;
    }
}
