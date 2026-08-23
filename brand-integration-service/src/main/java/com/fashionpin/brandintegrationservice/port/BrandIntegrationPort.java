package com.fashionpin.brandintegrationservice.port;

public interface BrandIntegrationPort {
    boolean syncBrandCatalog(String brandId, String providerName);
}
