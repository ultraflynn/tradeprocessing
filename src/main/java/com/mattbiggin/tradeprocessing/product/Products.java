package com.mattbiggin.tradeprocessing.product;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class Products {
    public static final String COLUMNS = "product_id,product_name";
    public static final String DEFAULT_PRODUCT_NAME = "Missing Product Name";
    private static final Logger log = LoggerFactory.getLogger(Products.class);
    final private ProductReader productReader;

    private final Map<String, String> products = new ConcurrentHashMap<>();

    @Autowired
    public Products(ProductReader productReader) {
        this.productReader = productReader;
    }

    private static boolean areProductsSortable(Set<String> productIds) {
        var sortable = true;
        for (var key : productIds) {
            try {
                Integer.parseInt(key);
            } catch (NumberFormatException e) {
                sortable = false;
            }
        }
        return sortable;
    }

    @PostConstruct
    public void init() {
        products.putAll(productReader.readProducts());
    }

    public String lookupProduct(String id) {
        if (products.containsKey(id)) {
            return products.get(id);
        } else {
            log.error("Missing product mapping - " + id);
            return DEFAULT_PRODUCT_NAME;
        }
    }

    public ProductList getProducts() {
        // Defensively copy the current product list so that sortability is accurate
        var copy = products.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new ProductList(copy, areProductsSortable(copy.keySet()));
    }

    public boolean addProduct(String id, String name) {
        if (products.containsKey(id)) {
            log.warn("Product id " + id + " already present");
            return false;
        } else {
            products.put(id, name);
            return true;
        }
    }

    public boolean changeProduct(String id, String name) {
        if (products.containsKey(id)) {
            products.replace(id, name);
            return true;
        } else {
            log.warn("Product id " + id + " not found for replacement");
            return false;
        }
    }

    public boolean removeProduct(String id) {
        if (products.containsKey(id)) {
            products.remove(id);
            return true;
        } else {
            log.warn("Product id " + id + " not found for removal");
            return false;
        }
    }
}
