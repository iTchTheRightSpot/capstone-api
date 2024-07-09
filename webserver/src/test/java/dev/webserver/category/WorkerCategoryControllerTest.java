package dev.webserver.category;

import com.github.javafaker.Faker;
import dev.webserver.AbstractIntegration;
import dev.webserver.data.TestData;
import dev.webserver.exception.DuplicateException;
import dev.webserver.exception.ResourceAttachedException;
import dev.webserver.product.WorkerProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class WorkerCategoryControllerTest extends AbstractIntegration {

    @Value(value = "/${api.endpoint.baseurl}worker/category")
    private String path;

    @Autowired
    private WorkerProductService service;
    @Autowired
    private CategoryRepository repository;

    void dummy() {
        var category = repository
                .save(
                        ProductCategory.builder()
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
                        ProductCategory.builder()
                                .name("clothes")
                                .isVisible(true)
                                .parentCategory(category)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        TestData.dummyProducts(clothes, 5, service);
    }

    private ProductCategory category() {
        var list = this.repository.findAll();
        assertFalse(list.isEmpty());
        return list.getFirst();
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void allCategories() throws Exception {
        dummy();

        this.mockMvc
                .perform(get(path).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.table").isArray())
                .andExpect(jsonPath("$.hierarchy").isArray());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void allProductsByCategoryId() throws Exception {
        var category = repository
                .save(
                        ProductCategory.builder()
                                .name("category")
                                .isVisible(true)
                                .parentCategory(null)
                                .categories(new HashSet<>())
                                .product(new HashSet<>())
                                .build()
                );

        TestData.dummyProducts(category, 15, service);

        // Then
        MvcResult result = super.mockMvc
                .perform(get(path + "/products")
                        .param("category_id", String.valueOf(category.getCategoryId()))
                        .param("page", "0")
                        .param("size", "20")
                )
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk())
                .andReturn();

        super.mockMvc
                .perform(asyncDispatch(result))
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isNotEmpty())
                .andExpect(jsonPath("$.content.size()").value(15));
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void shouldSuccessfullyCreateACategoryWhenParentIdDoesNotExists() throws Exception {
        dummy();

        // Given
        var dto = new CategoryDTO(new Faker().commerce().productName(), true, null);

        // Then
        this.mockMvc
                .perform(post(path)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void shouldSuccessfullyCreateACategoryWhenParentIdExists() throws Exception {
        dummy();

        // Given
        var dto = new CategoryDTO(new Faker().commerce().productName(), true, category().getCategoryId());

        // Then
        this.mockMvc
                .perform(post(path)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void shouldSuccessfullyUpdateACategory() throws Exception {
        dummy();

        // Given
        var category = this.repository.findAll().getFirst();
        var dto = new UpdateCategoryDTO(category.getCategoryId(), null, "Updated", category.isVisible());

        // Then
        this.mockMvc
                .perform(put(path)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void shouldThrowErrorDueToDuplicateCategoryName() throws Exception {
        dummy();

        // given
        var category = this.repository.findAll();
        var first = category.getFirst();
        var second = category.get(1);
        var dto = new UpdateCategoryDTO(first.getCategoryId(), null, second.getName(), first.isVisible());

        // then
        this.mockMvc
                .perform(put(path)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isConflict())
                .andExpect(result -> assertInstanceOf(DuplicateException.class, result.getResolvedException()));
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    @DisplayName(value = """
    exception thrown when trying to delete a product because it has a
    subcategory and product attached.
    """)
    void shouldThrowErrorAsCategoryHasOnDeleteRestrict() throws Exception {
        dummy();

        var category = this.repository
                .findById(category().getCategoryId())
                .orElse(null);
        assertNotNull(category);

        this.mockMvc
                .perform(MockMvcRequestBuilders.delete(path + "/{id}", category.getCategoryId())
                        .with(csrf())
                )
                .andExpect(status().isConflict())
                .andDo(result -> assertInstanceOf(ResourceAttachedException.class, result.getResolvedException()));
    }

}