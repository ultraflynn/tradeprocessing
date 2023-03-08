package com.mattbiggin.tradeprocessing;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@AutoConfigureMockMvc
class TradeProcessingEndpointTests {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testTradeEnrichment() throws Exception {
        MockHttpServletRequestBuilder builder =
                MockMvcRequestBuilders.post("/api/v1/enrich")
                        .contentType("text/csv")
                        .accept(MediaType.TEXT_PLAIN_VALUE)
                        .content(getTestTrades("src/test/resources/trade.csv"));

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("""
                        date,product_name,currency,price
                        20160101,Treasury Bills Domestic,EUR,10.0
                        20160101,Corporate Bonds Domestic,EUR,20.1
                        20160101,REPO Domestic,EUR,30.34
                        20160101,Missing Product Name,EUR,35.34
                        """))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void testTradeWithInvalidDate() throws Exception {
        MockHttpServletRequestBuilder builder =
                MockMvcRequestBuilders.post("/api/v1/enrich")
                        .contentType("text/csv")
                        .accept(MediaType.TEXT_PLAIN_VALUE)
                        .content(getTestTrades("src/test/resources/trade-invalid-date.csv"));

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("""
                        date,product_name,currency,price
                        20160101,Treasury Bills Domestic,EUR,10.0
                        20160101,Corporate Bonds Domestic,EUR,20.1
                        20160101,Missing Product Name,EUR,35.34
                        """))
                .andDo(MockMvcResultHandlers.print());
    }

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

    private String getTestTrades(String filename) {
        try (var lines = Files.lines(Path.of(filename))) {
            return lines.collect(Collectors.joining("\n"));
        } catch (IOException e) {
            fail(e.getMessage());
            return "";
        }
    }
}
