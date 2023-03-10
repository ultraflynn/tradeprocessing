package com.mattbiggin.tradeprocessing.trade;

import com.mattbiggin.tradeprocessing.product.Products;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeProcessorTest {
    @Mock
    private Products mockProducts;

    private final Set<String> missingProductMappings = new HashSet<>();

    @Test
    public void testProcessTrade() {
        final var processor = new TradeProcessor(mockProducts);
        when(mockProducts.lookupProduct("A", missingProductMappings)).thenReturn("Product Name");

        final var trade = processor.process("20160101,A,B,C", missingProductMappings);

        assertTrue(trade.isPresent());
        assertEquals("20160101,Product Name,B,C\n", trade.get());
    }

    @Test
    public void testInvalidColumnsCount() {
        final var processor = new TradeProcessor(mockProducts);

        final var trade = processor.process("20160101,A,B", missingProductMappings);

        assertFalse(trade.isPresent());
    }

    @Test
    public void testHandleInvalidDate() {
        final var processor = new TradeProcessor(mockProducts);
        when(mockProducts.lookupProduct("A", missingProductMappings)).thenReturn("Product Name");

        final var trade = processor.process("20161301,A,B,C", missingProductMappings);

        assertFalse(trade.isPresent());
    }
}