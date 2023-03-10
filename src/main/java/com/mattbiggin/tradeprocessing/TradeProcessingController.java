package com.mattbiggin.tradeprocessing;

import com.mattbiggin.tradeprocessing.product.Products;
import com.mattbiggin.tradeprocessing.trade.TradeStreamEnrichment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

@RequestMapping("/api/v1")
@RestController
public class TradeProcessingController {
    private static final Logger log = LoggerFactory.getLogger(TradeProcessingController.class);

    @Autowired
    private ConfigurationProperties properties;

    @Autowired
    private Products products;

    @Autowired
    private TradeStreamEnrichment tradeStreamEnrichment;

    @PostMapping("/enrich")
    public StreamingResponseBody enrich(InputStream trades) {
        log.info("POST /api/v1/enrich");

        final var reader = new BufferedReader(new InputStreamReader(trades, UTF_8));
        return outputStream -> tradeStreamEnrichment.processTrades(reader, outputStream);
    }

    @GetMapping("/products")
    public String products() {
        log.info("GET /api/v1/products");

        final var products = this.products.getProducts();
        final var productList = products.products()
                .entrySet().stream()
                .sorted((o1, o2) -> {
                    final var key1 = o1.getKey();
                    final var key2 = o2.getKey();

                    if (products.sortable()) {
                        return Integer.valueOf(key1).compareTo(Integer.valueOf(key2));
                    } else {
                        return key1.compareTo(key2);
                    }
                }) // Sort by product id assuming it
                .map(e -> e.getKey() + "," + e.getValue())
                .collect(Collectors.joining("\n"));
        return Products.COLUMNS + "\n" + productList + "\n";
    }

    @PutMapping("/products")
    public ResponseEntity<String> addProduct(@RequestParam("product_id") String id,
                                             @RequestParam("product_name") String name) {
        log.info("PUT /api/v1/products");

        if (products.addProduct(id.trim(), name.trim())) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>("product_id already present", HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("/products")
    public ResponseEntity<String> changeProduct(@RequestParam("product_id") String id,
                                                @RequestParam("product_name") String name) {
        log.info("PATCH /api/v1/products");

        if (products.changeProduct(id.trim(), name.trim())) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>("product_id not found", HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/products")
    public ResponseEntity<String> removeProduct(@RequestParam("product_id") String id) {
        log.info("DELETE /api/v1/products");

        if (products.removeProduct(id.trim())) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>("product_id not found", HttpStatus.BAD_REQUEST);
        }
    }
}