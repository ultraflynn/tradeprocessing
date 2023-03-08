package com.mattbiggin.tradeprocessing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConfigurationProperties {
    @Value("${static.productListFileName}")
    private String productList;

    public String getProductListFileName() {
        return productList;
    }
}
