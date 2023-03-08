package com.mattbiggin.tradeprocessing.trade;

import com.mattbiggin.tradeprocessing.product.Products;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;

@Component
public class TradeStreamEnrichment {
    private static final Logger log = LoggerFactory.getLogger(TradeStreamEnrichment.class);

    @Autowired
    private Products products;

    @Autowired
    private TradeProcessor tradeProcessor;

    public void processTrades(BufferedReader reader, OutputStream output) {
        final var productLookup = products.getProducts().products();

        try {
            output.write("date,product_name,currency,price\n".getBytes());

            var isFirst = true;
            var line = reader.readLine();
            while (line != null) {
                if (isFirst) { // Ignore the first header row
                    isFirst = false;
                } else {
                    tradeProcessor.process(line, productLookup).ifPresent(t -> {
                        try {
                            output.write(t.getBytes());
                        } catch (IOException e) {
                            log.error(e.getMessage());
                        }
                    });
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
