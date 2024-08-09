package dev.webserver.category;

import com.github.javafaker.Faker;
import dev.webserver.AbstractIntegration;
import dev.webserver.TestUtility;
import dev.webserver.data.TestData;
import dev.webserver.exception.DuplicateException;
import dev.webserver.exception.ResourceAttachedException;
import dev.webserver.product.WorkerProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                        Category.builder()
                                .name("category")
                                .isVisible(true)
                                .build());

        TestData.dummyProducts(category, 2, service);

        var clothes = repository.save(Category.builder().name("clothes").isVisible(true).build());

        TestData.dummyProducts(clothes, 5, service);
    }

    private Category category() {
        var list = TestUtility.toList(repository.findAll());
        assertFalse(list.isEmpty());
        return list.getFirst();
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void allCategories() throws Exception {
        dummy();

        super.mockMvc
                .perform(get(path).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.table").isArray())
                .andExpect(jsonPath("$.hierarchy").isArray());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void allProductsByCategoryId() throws Exception {
        var category = repository.save(Category.builder().name("category").isVisible(true).build());

        TestData.dummyProducts(category, 15, service);

        // Then
        super.mockMvc
                .perform(get(path + "/products")
                        .param("category_id", String.valueOf(category.categoryId()))
                        .param("page", "0")
                        .param("size", "20")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isNotEmpty())
                .andExpect(jsonPath("$.content.size()").value(15));
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void shouldSuccessfullyCreateACategoryWhenParentIdDoesNotExists() throws Exception {
        dummy();

        // Given
        var dto = new CategoryDto(new Faker().commerce().productName(), true, null);

        // Then
        super.mockMvc
                .perform(post(path)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(super.mapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void shouldSuccessfullyCreateACategoryWhenParentIdExists() throws Exception {
        dummy();

        // Given
        var dto = new CategoryDto(new Faker().commerce().productName(), true, category().categoryId());

        // Then
        super.mockMvc
                .perform(post(path)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(super.mapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void shouldSuccessfullyUpdateACategory() throws Exception {
        dummy();

        // Given
        var category = TestUtility.toList(repository.findAll()).getFirst();
        var dto = new UpdateCategoryDto(category.categoryId(), null, "Updated", category.isVisible());

        // Then
        super.mockMvc
                .perform(put(path)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(super.mapper.writeValueAsString(dto))
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", password = "password", roles = {"WORKER"})
    void shouldThrowErrorDueToDuplicateCategoryName() throws Exception {
        dummy();

        // given
        var category = TestUtility.toList(repository.findAll());
        var first = category.getFirst();
        var second = category.get(1);
        var dto = new UpdateCategoryDto(first.categoryId(), null, second.name(), first.isVisible());

        // then
        super.mockMvc
                .perform(put(path)
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(super.mapper.writeValueAsString(dto))
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

        var category = this.repository.findById(category().categoryId()).orElse(null);
        assertNotNull(category);

        super.mockMvc
                .perform(MockMvcRequestBuilders.delete(path + "/{id}", category.categoryId())
                        .with(csrf())
                )
                .andExpect(status().isConflict())
                .andDo(result -> assertInstanceOf(ResourceAttachedException.class, result.getResolvedException()));
    }

}