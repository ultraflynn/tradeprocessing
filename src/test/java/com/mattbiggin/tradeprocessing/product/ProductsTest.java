package com.mattbiggin.tradeprocessing.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductsTest {
    @Mock
    private ProductReader mockReader;

    private final Set<String> missingProductMappings = new HashSet<>();

    private Products products;

    @BeforeEach
    public void init() {
        products = new Products(mockReader);
    }

    @Test
    public void testReadProducts() {
        products.init();
        verify(mockReader).readProducts();
    }

    @Test
    public void testLookupMissingProduct() {
        final var name = products.lookupProduct("1", missingProductMappings);

        assertEquals("Missing Product Name", name);
        assertTrue(missingProductMappings.contains("1"));
    }

    @Test
    public void testLookupMissingProductDoesNotAddDuplicate() {
        var missingProduct = Set.of("1"); // Immutable
        final var name = products.lookupProduct("1", missingProduct);

        // Had duplicate been added the immutable Set would have triggered an exception
        assertEquals("Missing Product Name", name);
    }

    @Test
    public void testAddProduct() {
        assertTrue(products.addProduct("1", "Product A"));
        final var name = products.lookupProduct("1", missingProductMappings);

        assertEquals("Product A", name);
    }

    @Test
    public void testRemoveProductAndGetMissingProduct() {
        assertTrue(products.addProduct("1", "Product A"));
        assertTrue(products.removeProduct("1"));
        final var name = products.lookupProduct("1", missingProductMappings);

        assertEquals("Missing Product Name", name);
    }

    @Test
    public void testGetProducts() {
        assertTrue(products.addProduct("1", "Product A"));
        assertTrue(products.addProduct("2", "Product B"));

        final var productList = products.getProducts();
        assertEquals(2, productList.products().size());
        assertTrue(productList.sortable());
    }

    @Test
    public void testGetUnsortableProducts() {
        assertTrue(products.addProduct("ONE", "Product A"));
        assertTrue(products.addProduct("TWO", "Product B"));

        final var productList = products.getProducts();
        assertEquals(2, productList.products().size());
        assertFalse(productList.sortable());
    }

    @Test
    public void testChangeProduct() {
        assertTrue(products.addProduct("1", "Product A"));
        assertTrue(products.changeProduct("1", "Product B"));
    }

    @Test
    public void testChangeMissingProduct() {
        assertFalse(products.changeProduct("1", "Product A"));
    }

    @Test
    public void testAddDuplicateProduct() {
        assertTrue(products.addProduct("1", "Product A"));
        assertFalse(products.addProduct("1", "Product B"));
    }

    @Test
    public void testRemoveMissingProduct() {
        assertFalse(products.removeProduct("1"));
    }
}