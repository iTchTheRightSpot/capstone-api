package com.sarabrandserver.product.controller;

import com.sarabrandserver.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ClientProductControllerTest extends AbstractIntegrationTest {

    final String path = "/api/v1/client/product";

    @Test
    void search_functionality() throws Exception {
        var list = this.productRepo.findAll();
        assertFalse(list.isEmpty());
        String name = list.get(0).getName();

        this.MOCKMVC
                .perform(get(path + "/find")
                        .param("search", name)
                        .param("currency", "ngn")
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void list_products_ngn_currency() throws Exception {
        this.MOCKMVC
                .perform(get(path))
                .andExpect(status().isOk());
    }

    @Test
    void list_products_usd_currency() throws Exception {
        this.MOCKMVC
                .perform(get(path).param("currency", "usd"))
                .andExpect(status().isOk());
    }

    @Test
    void fetchProductDetails() throws Exception {
        var list = this.productRepo.findAll();
        assertFalse(list.isEmpty());
        String productID = list.get(0).getUuid();

        this.MOCKMVC
                .perform(get(path + "/detail")
                        .param("product_id", productID)
                )
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].variants").isArray())
                .andExpect(jsonPath("$[*].variants.length()").value(this.detailSize));
    }

}