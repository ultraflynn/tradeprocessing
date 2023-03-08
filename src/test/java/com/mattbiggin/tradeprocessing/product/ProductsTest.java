package com.mattbiggin.tradeprocessing.product;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductsTest {
    @Mock
    private ProductReader mockReader;

    @Test
    public void testReadProducts() {
        final var products = new Products(mockReader);

        products.init();
        verify(mockReader).readProducts();
    }

    @Test
    public void testAddProduct() {
        final var products = new Products(mockReader);

        assertTrue(products.addProduct("1", "Product A"));
        final var name = products.lookupProduct("1");

        assertEquals("Product A", name);
    }

    @Test
    public void testRemoveProductAndGetMissingProduct() {
        final var products = new Products(mockReader);

        assertTrue(products.addProduct("1", "Product A"));
        assertTrue(products.removeProduct("1"));
        final var name = products.lookupProduct("1");

        assertEquals("Missing Product Name", name);
    }

    @Test
    public void testGetProducts() {
        final var products = new Products(mockReader);

        assertTrue(products.addProduct("1", "Product A"));
        assertTrue(products.addProduct("2", "Product B"));

        final var productList = products.getProducts();
        assertEquals(2, productList.products().size());
        assertTrue(productList.sortable());
    }

    @Test
    public void testGetUnsortableProducts() {
        final var products = new Products(mockReader);

        assertTrue(products.addProduct("ONE", "Product A"));
        assertTrue(products.addProduct("TWO", "Product B"));

        final var productList = products.getProducts();
        assertEquals(2, productList.products().size());
        assertFalse(productList.sortable());
    }

    @Test
    public void testChangeProduct() {
        final var products = new Products(mockReader);

        assertTrue(products.addProduct("1", "Product A"));
        assertTrue(products.changeProduct("1", "Product B"));
    }

    @Test
    public void testChangeMissingProduct() {
        final var products = new Products(mockReader);

        assertFalse(products.changeProduct("1", "Product A"));
    }

    @Test
    public void testAddDuplicateProduct() {
        final var products = new Products(mockReader);

        assertTrue(products.addProduct("1", "Product A"));
        assertFalse(products.addProduct("1", "Product B"));
    }

    @Test
    public void testRemoveMissingProduct() {
        final var products = new Products(mockReader);

        assertFalse(products.removeProduct("1"));
    }
}