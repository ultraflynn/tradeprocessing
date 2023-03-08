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
class EnrichmentEndpointTest {
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

    private String getTestTrades(String filename) {
        try (var lines = Files.lines(Path.of(filename))) {
            return lines.collect(Collectors.joining("\n"));
        } catch (IOException e) {
            fail(e.getMessage());
            return "";
        }
    }
}
