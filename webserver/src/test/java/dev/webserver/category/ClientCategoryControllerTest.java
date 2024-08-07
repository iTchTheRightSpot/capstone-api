package dev.webserver.category;

import dev.webserver.AbstractIntegration;
import dev.webserver.data.TestData;
import dev.webserver.product.WorkerProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashSet;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ClientCategoryControllerTest extends AbstractIntegration {

    @Value(value = "/${api.endpoint.baseurl}client/category")
    private String path;

    @Autowired
    private WorkerProductService service;
    @Autowired
    private CategoryRepository repository;

    @Test
    void allCategories() throws Exception {
        var category = repository
                .save(
                        Category.builder()
                                .name("category")
                                .isVisible(true)
                                .parentCategory(null)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        TestData.dummyProducts(category, 2, service);

        var clothes = repository
                .save(
                        Category.builder()
                                .name("clothes")
                                .isVisible(true)
                                .parentCategory(category)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        TestData.dummyProducts(clothes, 5, service);

        this.mockMvc
                .perform(get(path).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].category", notNullValue()))
                .andExpect(jsonPath("$[*].category_id", notNullValue()));
    }

    @Test
    void allProductsByCategoryId() throws Exception {
        var category = repository
                .save(
                        Category.builder()
                                .name("category")
                                .isVisible(true)
                                .parentCategory(null)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        TestData.dummyProducts(category, 10, service);

        super.mockMvc
                .perform(get(path + "/products").param("category_id", String.valueOf(category.getCategoryId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isNotEmpty())
                .andExpect(jsonPath("$.content.size()").value(10));
    }

}