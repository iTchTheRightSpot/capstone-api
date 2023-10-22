package com.sarabrandserver.product.controller;

import com.sarabrandserver.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ClientProductControllerTest extends AbstractIntegrationTest {

    @Test
    void fetchProductDetails() throws Exception {
        var list = this.productRepo.findAll();
        assertFalse(list.isEmpty());

        String productID = list.get(0).getUuid();

//        MvcResult result = MOCKMVC
//                .perform(get(requestMapping + "/detail")
//                        .param("product_id", productID)
//                )
//                .andExpect(request().asyncStarted())
//                .andDo(MockMvcResultHandlers.log())
//                .andReturn();

//        MOCKMVC
//                .perform(asyncDispatch(result))
//                .andExpect(status().isOk())
//                .andDo(print())
//                .andExpect(jsonPath("$").isArray())
//                .andExpect(jsonPath("$[*].variants").isArray())
//                .andExpect(jsonPath("$[*].variants.length()").value(this.detailSize));

        this.MOCKMVC
                .perform(get("/api/v1/client/product/detail")
                                .param("product_id", productID)
                )
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].variants").isArray())
                .andExpect(jsonPath("$[*].variants.length()").value(this.detailSize));
    }

}