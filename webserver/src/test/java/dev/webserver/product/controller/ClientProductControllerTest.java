package dev.webserver.product.controller;

import dev.webserver.AbstractIntegration;
import dev.webserver.category.entity.ProductCategory;
import dev.webserver.category.repository.CategoryRepository;
import dev.webserver.data.TestData;
import dev.webserver.product.repository.ProductRepo;
import dev.webserver.product.service.WorkerProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashSet;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ClientProductControllerTest extends AbstractIntegration {

    @Value(value = "/${api.endpoint.baseurl}client/product")
    private String path;

    @Autowired
    private WorkerProductService workerProductService;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private CategoryRepository categoryRepository;

    private void dummy() {
        var category = categoryRepository
                .save(
                        ProductCategory.builder()
                                .name("category")
                                .isVisible(true)
                                .parentCategory(null)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        TestData.dummyProducts(category, 2, workerProductService);

        var clothes = categoryRepository
                .save(
                        ProductCategory.builder()
                                .name("clothes")
                                .isVisible(true)
                                .parentCategory(category)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        TestData.dummyProducts(clothes, 5, workerProductService);
    }

    @Test
    void searchFunctionality() throws Exception {
        dummy();

        char c = (char) ('a' + new Random().nextInt(26));

        MvcResult result = super.mockMvc
                .perform(get(path + "/find")
                        .param("search", String.valueOf(c))
                        .param("currency", "usd")
                        .param("size", "10")
                )
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk())
                .andReturn();

        super.mockMvc
                .perform(asyncDispatch(result))
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void listProductsNgnCurrency() throws Exception {
        dummy();

        MvcResult result = super.mockMvc
                .perform(get(path))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk())
                .andReturn();

        super.mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk());
    }

    @Test
    void listProductsUsdCurrency() throws Exception {
        dummy();

        MvcResult result = super.mockMvc
                .perform(get(path)
                        .param("currency", "usd")
                )
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk())
                .andReturn();

        super.mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnProductDetails() throws Exception {
        dummy();

        var list = this.productRepo.findAll();
        assertFalse(list.isEmpty());
        String id = list.getFirst().getUuid();

        MvcResult result = super.mockMvc
                .perform(get(path + "/detail")
                        .param("product_id", id)
                )
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk())
                .andReturn();

        super.mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk());
    }

}