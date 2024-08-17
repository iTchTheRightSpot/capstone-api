package dev.webserver.category;

import com.github.javafaker.Faker;
import dev.webserver.AbstractIntegration;
import dev.webserver.TestData;
import dev.webserver.TestUtility;
import dev.webserver.exception.DuplicateException;
import dev.webserver.exception.ResourceAttachedException;
import dev.webserver.product.EmployeeProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

final class EmployeeCategoryControllerTest extends AbstractIntegration {

    @Value(value = "/${api.endpoint.baseurl}employee/category")
    private String path;

    @Autowired
    private EmployeeProductService service;
    @Autowired
    private CategoryRepository repository;

    void dummy() {
        final var category = repository.save(Category.builder().name("category").isVisible(true).build());

        TestData.dummyProducts(category, 2, service);

        final var clothes = repository.save(Category.builder().name("clothes").isVisible(true).build());

        TestData.dummyProducts(clothes, 5, service);
    }

    private Category category() {
        final var list = TestUtility.toList(repository.findAll());
        assertFalse(list.isEmpty());
        return list.getFirst();
    }

    @Test
    @WithMockUser(username = "admin@admin.com", roles = {"EMPLOYEE"})
    void allCategories() throws Exception {
        final var category = repository.save(Category.builder().name("category").isVisible(true).build());
        repository.save(Category.builder().name("men").parentId(category.categoryId()).isVisible(false).build());
        repository.save(Category.builder().name("collection").isVisible(true).build());

        super.mockMvc
                .perform(get(path).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*]", hasSize(3)))

                .andExpect(jsonPath("$[0].category_id", notNullValue()))
                .andExpect(jsonPath("$[0].name").value("category"))
                .andExpect(jsonPath("$[0].visible").value(true))

                .andExpect(jsonPath("$[1].category_id", notNullValue()))
                .andExpect(jsonPath("$[1].name").value("men"))
                .andExpect(jsonPath("$[1].visible").value(false))
                .andExpect(jsonPath("$[1].parent_id", notNullValue()))

                .andExpect(jsonPath("$[2].category_id", notNullValue()))
                .andExpect(jsonPath("$[2].name").value("collection"))
                .andExpect(jsonPath("$[2].visible").value(true));
    }

    @Test
    @WithMockUser(username = "admin@admin.com", roles = {"EMPLOYEE"})
    void allProductsByCategoryId() throws Exception {
        final var category = repository.save(Category.builder().name("category").isVisible(true).build());

        TestData.dummyProducts(category, 15, service);

        // Then
        super.mockMvc
                .perform(get(path + "/products")
                        .param("category_id", String.valueOf(category.categoryId()))
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_pages").value(3))
                .andExpect(jsonPath("$.total_elements").value(15))
                .andExpect(jsonPath("$.number_of_elements").value(5))
                .andExpect(jsonPath("$.has_previous_page").value(false))
                .andExpect(jsonPath("$.has_next_page").value(true))
                .andExpect(jsonPath("$.is_empty").value(false))
                .andExpect(jsonPath("$.data[*]", hasSize(5)));
    }

    @Test
    @WithMockUser(username = "admin@admin.com", roles = {"EMPLOYEE"})
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
    @WithMockUser(username = "admin@admin.com", roles = {"EMPLOYEE"})
    void shouldSuccessfullyCreateACategoryWhenParentIdExists() throws Exception {
        dummy();

        // Given
        final var dto = new CategoryDto(new Faker().commerce().productName(), true, category().categoryId());

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
    @WithMockUser(username = "admin@admin.com", roles = {"EMPLOYEE"})
    void shouldSuccessfullyUpdateACategory() throws Exception {
        // given
        final var category = repository.save(Category.builder().name("category").isVisible(true).build());
        final var dto = super.mapper.writeValueAsString(
                new UpdateCategoryDto(category.categoryId(), null, "Updated", false));

        // request
        super.mockMvc.perform(put(path).with(csrf()).contentType(APPLICATION_JSON).content(dto))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin@admin.com", roles = {"EMPLOYEE"})
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
    @WithMockUser(username = "admin@admin.com", roles = {"EMPLOYEE"})
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