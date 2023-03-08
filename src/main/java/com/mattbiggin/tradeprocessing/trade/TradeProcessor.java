package com.mattbiggin.tradeprocessing.trade;

import com.mattbiggin.tradeprocessing.product.Products;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;

@Component
class TradeProcessor {
    private static final Logger log = LoggerFactory.getLogger(TradeProcessor.class);

    final private Products products;

    @Autowired
    TradeProcessor(Products products) {
        this.products = products;
    }

    Optional<String> process(String line) {
        final var columns = line.split(",");

        if (hasValidColumns(columns)) {
            final String date = columns[0].trim();
            final String productId = columns[1].trim();
            final String currency = columns[2].trim();
            final String price = columns[3].trim();

            final var productName = products.lookupProduct(productId);

            if (isValidDate(date, line)) {
                return Optional.of(String.join(",", date, productName, currency, price) + "\n");
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    private boolean hasValidColumns(String[] trade) {
        return trade.length == 4;
    }

    private boolean isValidDate(String date, String trade) {
        var dateFormatter = DateTimeFormatter.BASIC_ISO_DATE;

        try {
            LocalDate.parse(date, dateFormatter);
            return true;
        } catch (DateTimeParseException e) {
            log.error("Invalid date, ignoring trade - " + trade);
            return false;
        }
    }
}
