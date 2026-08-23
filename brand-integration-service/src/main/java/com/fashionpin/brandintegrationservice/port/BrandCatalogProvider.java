package com.fashionpin.brandintegrationservice.port;

public interface BrandCatalogProvider {
    String getProviderName();
    boolean fetchProducts(String brandId);
}
