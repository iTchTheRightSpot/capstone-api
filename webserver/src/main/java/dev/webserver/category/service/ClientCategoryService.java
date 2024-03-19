package dev.webserver.category.service;

import dev.webserver.aws.S3Service;
import dev.webserver.category.entity.ProductCategory;
import dev.webserver.category.projection.CategoryPojo;
import dev.webserver.category.repository.CategoryRepository;
import dev.webserver.category.response.CategoryResponse;
import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.product.projection.ProductPojo;
import dev.webserver.product.response.ProductResponse;
import dev.webserver.util.CustomUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class ClientCategoryService {

    @Value(value = "${aws.bucket}")
    private String BUCKET;

    private final CategoryRepository repository;
    private final S3Service service;

    /**
     * Returns a list of {@code CategoryResponse}
     * with is_visible marked as true.
     * */
    public List<CategoryResponse> allCategories() {
        var list = this.repository
                .allCategories()
                .stream()
                .filter(CategoryPojo::statusImpl)
                .map(p -> new CategoryResponse(p.getId(), p.getParent(), p.getName(), p.statusImpl()))
                .toList();

        return CustomUtil.createCategoryHierarchy(list);
    }

    /**
     * Asynchronously retrieves a {@link Page} of
     * {@link ProductResponse} objects associated with a
     * specific category.
     *
     * @param currency    The currency in which prices are displayed.
     * @param categoryId  The primary key of a {@link ProductCategory}.
     * @param page        The page number for pagination.
     * @param size        The page size for pagination.
     * @return A {@link CompletableFuture} representing a {@link Page}
     * of {@link ProductResponse}.
     */
    public CompletableFuture<Page<ProductResponse>> allProductsByCategoryId(
            SarreCurrency currency,
            long categoryId,
            int page,
            int size
    ) {
        var pageOfProducts = this.repository
                .allProductsByCategoryIdWhereInStockAndIsVisible(categoryId, currency, PageRequest.of(page, size));

        var futures = createTasks(pageOfProducts);

        return CustomUtil.asynchronousTasks(futures, ClientCategoryService.class)
                .thenApply(v -> new PageImpl<>(
                        v.stream().map(Supplier::get).toList(),
                        pageOfProducts.getPageable(),
                        pageOfProducts.getTotalElements()
                ));
    }

    private List<Supplier<ProductResponse>> createTasks(Page<ProductPojo> page) {
        return page.stream()
                .map(p -> (Supplier<ProductResponse>) () -> new ProductResponse(
                        p.getUuid(),
                        p.getName(),
                        p.getDescription(),
                        p.getPrice(),
                        p.getCurrency(),
                        service.preSignedUrl(BUCKET, p.getImage())
                ))
                .toList();
    }

}