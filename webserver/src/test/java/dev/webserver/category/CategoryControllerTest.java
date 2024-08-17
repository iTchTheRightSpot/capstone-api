package dev.webserver.category;

import dev.webserver.AbstractIntegration;
import dev.webserver.TestData;
import dev.webserver.product.EmployeeProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

final class CategoryControllerTest extends AbstractIntegration {

    @Value(value = "/${api.endpoint.baseurl}category")
    private String path;

    @Autowired
    private EmployeeProductService service;
    @Autowired
    private CategoryRepository repository;

    @Test
    void allCategories() throws Exception {
        repository.save(Category.builder().name("category").isVisible(true).build());
        repository.save(Category.builder().name("clothes").isVisible(true).build());

        super.mockMvc
                .perform(get(path).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*]", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("category"))
                .andExpect(jsonPath("$[1].name").value("clothes"))
                .andExpect(jsonPath("$[0].category_id", notNullValue()))
                .andExpect(jsonPath("$[1].category_id", notNullValue()));
    }

    @Test
    void allProductsByCategoryId() throws Exception {
        final var category = repository.save(Category.builder().name("category").isVisible(true).build());

        TestData.dummyProducts(category, 21, service);

        super.mockMvc
                .perform(get(path + "/products").param("category_id", String.valueOf(category.categoryId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_pages").value(2))
                .andExpect(jsonPath("$.total_elements").value(21))
                .andExpect(jsonPath("$.number_of_elements").value(20))
                .andExpect(jsonPath("$.has_previous_page").value(false))
                .andExpect(jsonPath("$.has_next_page").value(true))
                .andExpect(jsonPath("$.is_empty").value(false))
                .andExpect(jsonPath("$.data[*]", hasSize(20)));
    }

}