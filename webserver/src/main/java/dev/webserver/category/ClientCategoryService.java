package dev.webserver.category;

import dev.webserver.enumeration.SarreCurrency;
import dev.webserver.external.aws.S3Service;
import dev.webserver.product.ProductResponse;
import dev.webserver.util.CustomUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
class ClientCategoryService {

    @Value(value = "${aws.bucket}")
    private String bucket;

    private final CategoryRepository categoryRepository;
    private final S3Service s3Service;

    /**
     * Returns a {@link List} of {@link CategoryResponse}.
     * */
    public List<CategoryResponse> allCategories() {
        var list = this.categoryRepository
                .allCategories()
                .stream()
                .filter(CategoryProjection::statusImpl)
                .map(p -> new CategoryResponse(p.getId(), p.getParent(), p.getName(), p.statusImpl()))
                .toList();

        return CustomUtil.createCategoryHierarchy(list);
    }

    /**
     * Asynchronously retrieves a {@link Page} of {@link ProductResponse}
     * objects by a {@link ProductCategory}.
     *
     * @param currency    The currency in which prices are displayed.
     * @param categoryId  The primary key of a {@link ProductCategory}.
     * @param page        The page number for pagination.
     * @param size        The page size for pagination.
     * @return A {@link Page} of {@link ProductResponse}.
     */
    public Page<ProductResponse> allProductsByCategoryId(
            final SarreCurrency currency,
            final long categoryId,
            final int page,
            final int size
    ) {
        var pageOfProducts = categoryRepository
                .allProductsByCategoryIdWhereInStockAndIsVisible(categoryId, currency, PageRequest.of(page, size));

        var futures = pageOfProducts.stream()
                .map(p -> (Supplier<ProductResponse>) () -> ProductResponse.builder()
                        .id(p.getUuid())
                        .name(p.getName())
                        .desc(p.getDescription())
                        .price(p.getPrice())
                        .currency(p.getCurrency())
                        .imageUrl(p.getImage())
                        .build())
                .toList();

        final var products = CustomUtil.asynchronousTasks(futures).join();
        return new PageImpl<>(products, pageOfProducts.getPageable(), pageOfProducts.getTotalElements());
    }

}