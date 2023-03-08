package com.mattbiggin.tradeprocessing.trade;

import com.mattbiggin.tradeprocessing.product.Products;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeProcessorTest {
    @Mock
    private Products mockProducts;

    @Test
    public void testProcessTrade() {
        final var processor = new TradeProcessor(mockProducts);
        when(mockProducts.lookupProduct("A")).thenReturn("Product Name");

        final var trade = processor.process("20160101,A,B,C");

        assertTrue(trade.isPresent());
        assertEquals("20160101,Product Name,B,C\n", trade.get());
    }

    @Test
    public void testInvalidColumnsCount() {
        final var processor = new TradeProcessor(mockProducts);

        final var trade = processor.process("20160101,A,B");

        assertFalse(trade.isPresent());
    }

    @Test
    public void testHandleInvalidDate() {
        final var processor = new TradeProcessor(mockProducts);
        when(mockProducts.lookupProduct("A")).thenReturn("Product Name");

        final var trade = processor.process("20161301,A,B,C");

        assertFalse(trade.isPresent());
    }
}