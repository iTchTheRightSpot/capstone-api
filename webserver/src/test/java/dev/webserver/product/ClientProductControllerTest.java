package dev.webserver.product;

import dev.webserver.AbstractIntegration;
import dev.webserver.category.CategoryRepository;
import dev.webserver.category.ProductCategory;
import dev.webserver.data.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashSet;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ClientProductControllerTest extends AbstractIntegration {

    @Value(value = "/${api.endpoint.baseurl}client/product")
    private String path;

    @Autowired
    private WorkerProductService workerProductService;
    @Autowired
    private ProductRepository productRepository;
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

        super.mockMvc
                .perform(get(path + "/find")
                        .param("search", String.valueOf(c))
                        .param("currency", "usd")
                        .param("size", "10")
                )
                .andExpect(status().isOk());
    }

    @Test
    void listProductsNgnCurrency() throws Exception {
        dummy();

        super.mockMvc.perform(get(path)).andExpect(status().isOk());
    }

    @Test
    void listProductsUsdCurrency() throws Exception {
        dummy();

        super.mockMvc.perform(get(path).param("currency", "usd")).andExpect(status().isOk());
    }

    @Test
    void shouldReturnProductDetails() throws Exception {
        dummy();

        var list = this.productRepository.findAll();
        assertFalse(list.isEmpty());
        String id = list.getFirst().getUuid();

        super.mockMvc.perform(get(path + "/detail").param("product_id", id)).andExpect(status().isOk());
    }

}