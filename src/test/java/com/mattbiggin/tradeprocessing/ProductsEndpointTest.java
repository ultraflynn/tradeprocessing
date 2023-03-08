package com.mattbiggin.tradeprocessing;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@AutoConfigureMockMvc
class ProductsEndpointTest {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetDefaultProductList() throws Exception {
        MockHttpServletRequestBuilder get =
                MockMvcRequestBuilders.get("/api/v1/products");

        mockMvc.perform(get)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("""
                        product_id,product_name
                        1,Treasury Bills Domestic
                        2,Corporate Bonds Domestic
                        3,REPO Domestic
                        4,Interest rate swaps International
                        5,OTC Index Option
                        6,Currency Options
                        7,Reverse Repos International
                        8,REPO International
                        9,766A_CORP BD
                        10,766B_CORP BD
                        """))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void testGetUnsortableProductList() throws Exception {
        MockHttpServletRequestBuilder put =
                MockMvcRequestBuilders.put("/api/v1/products")
                        .queryParam("product_id", "unsortable-key")
                        .queryParam("product_name", "Credit Default Swap");

        MockHttpServletRequestBuilder get =
                MockMvcRequestBuilders.get("/api/v1/products");

        MockHttpServletRequestBuilder delete =
                MockMvcRequestBuilders.delete("/api/v1/products")
                        .queryParam("product_id", "unsortable-key");

        mockMvc.perform(put)
                .andExpect(MockMvcResultMatchers.status().isOk());

        mockMvc.perform(get)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("""
                        product_id,product_name
                        1,Treasury Bills Domestic
                        10,766B_CORP BD
                        2,Corporate Bonds Domestic
                        3,REPO Domestic
                        4,Interest rate swaps International
                        5,OTC Index Option
                        6,Currency Options
                        7,Reverse Repos International
                        8,REPO International
                        9,766A_CORP BD
                        unsortable-key,Credit Default Swap
                        """))
                .andDo(MockMvcResultHandlers.print());

        mockMvc.perform(delete)
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    /*
     * Both add and delete are tested here so that the product list is returned to its original state
     * for other tests.
     */
    @Test
    void testAddAndDeleteNewProduct() throws Exception {
        MockHttpServletRequestBuilder put =
                MockMvcRequestBuilders.put("/api/v1/products")
                        .queryParam("product_id", "11")
                        .queryParam("product_name", "Credit Default Swap");

        MockHttpServletRequestBuilder get =
                MockMvcRequestBuilders.get("/api/v1/products");

        MockHttpServletRequestBuilder delete =
                MockMvcRequestBuilders.delete("/api/v1/products")
                        .queryParam("product_id", "11");

        mockMvc.perform(put)
                .andExpect(MockMvcResultMatchers.status().isOk());

        mockMvc.perform(get)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("""
                        product_id,product_name
                        1,Treasury Bills Domestic
                        2,Corporate Bonds Domestic
                        3,REPO Domestic
                        4,Interest rate swaps International
                        5,OTC Index Option
                        6,Currency Options
                        7,Reverse Repos International
                        8,REPO International
                        9,766A_CORP BD
                        10,766B_CORP BD
                        11,Credit Default Swap
                        """))
                .andDo(MockMvcResultHandlers.print());

        mockMvc.perform(delete)
                .andExpect(MockMvcResultMatchers.status().isOk());


        mockMvc.perform(get)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("""
                        product_id,product_name
                        1,Treasury Bills Domestic
                        2,Corporate Bonds Domestic
                        3,REPO Domestic
                        4,Interest rate swaps International
                        5,OTC Index Option
                        6,Currency Options
                        7,Reverse Repos International
                        8,REPO International
                        9,766A_CORP BD
                        10,766B_CORP BD
                        """))
                .andDo(MockMvcResultHandlers.print());
    }

    /*
     * Change to the product name is reverted to return it to its original state for other tests.
     */
    @Test
    void testChangeAndRevertProductName() throws Exception {
        MockHttpServletRequestBuilder change =
                MockMvcRequestBuilders.patch("/api/v1/products")
                        .queryParam("product_id", "2")
                        .queryParam("product_name", "Credit Default Swap");

        MockHttpServletRequestBuilder get =
                MockMvcRequestBuilders.get("/api/v1/products");

        MockHttpServletRequestBuilder revert =
                MockMvcRequestBuilders.patch("/api/v1/products")
                        .queryParam("product_id", "2")
                        .queryParam("product_name", "Corporate Bonds Domestic");

        mockMvc.perform(change)
                .andExpect(MockMvcResultMatchers.status().isOk());

        mockMvc.perform(get)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("""
                        product_id,product_name
                        1,Treasury Bills Domestic
                        2,Credit Default Swap
                        3,REPO Domestic
                        4,Interest rate swaps International
                        5,OTC Index Option
                        6,Currency Options
                        7,Reverse Repos International
                        8,REPO International
                        9,766A_CORP BD
                        10,766B_CORP BD
                        """))
                .andDo(MockMvcResultHandlers.print());

        mockMvc.perform(revert)
                .andExpect(MockMvcResultMatchers.status().isOk());


        mockMvc.perform(get)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("""
                        product_id,product_name
                        1,Treasury Bills Domestic
                        2,Corporate Bonds Domestic
                        3,REPO Domestic
                        4,Interest rate swaps International
                        5,OTC Index Option
                        6,Currency Options
                        7,Reverse Repos International
                        8,REPO International
                        9,766A_CORP BD
                        10,766B_CORP BD
                        """))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void testAddExistingProductName() throws Exception {
        MockHttpServletRequestBuilder put =
                MockMvcRequestBuilders.put("/api/v1/products")
                        .queryParam("product_id", "2")
                        .queryParam("product_name", "Credit Default Swap");

        mockMvc.perform(put)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("product_id already present"));
    }

    @Test
    void testChangeMissingProductName() throws Exception {
        MockHttpServletRequestBuilder change =
                MockMvcRequestBuilders.patch("/api/v1/products")
                        .queryParam("product_id", "11")
                        .queryParam("product_name", "Credit Default Swap");

        mockMvc.perform(change)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("product_id not found"));
    }

    @Test
    void testRemoveMissingProductName() throws Exception {
        MockHttpServletRequestBuilder delete =
                MockMvcRequestBuilders.delete("/api/v1/products")
                        .queryParam("product_id", "11")
                        .queryParam("product_name", "Credit Default Swap");

        mockMvc.perform(delete)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("product_id not found"));
    }
}
