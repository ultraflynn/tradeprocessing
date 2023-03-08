package com.mattbiggin.tradeprocessing.product;

import com.mattbiggin.tradeprocessing.ConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
class ResourceFileProductReader implements ProductReader {
    private static final Logger log = LoggerFactory.getLogger(ResourceFileProductReader.class);

    @Autowired
    private ConfigurationProperties properties;

    @Override
    public Map<String, String> readProducts() {
        final var productList = properties.getProductListFileName();
        final var products = new HashMap<String, String>();

        try (Stream<String> lines = Files.lines(Paths.get(productList))) {
            products.putAll(lines.skip(1)
                    .map(line -> line.split(","))
                    .collect(Collectors.toMap(line -> line[0].trim(), line -> line[1].trim())));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return products;
    }
}
