package dev.webserver.category;

import dev.webserver.AbstractEnvironment;
import dev.webserver.enumeration.CapstoneCurrency;
import dev.webserver.external.aws.IS3Service;
import dev.webserver.product.ProductResponse;
import dev.webserver.util.CustomUtil;
import dev.webserver.util.Page;
import dev.webserver.util.Pageable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

@Service
class CategoryService extends AbstractEnvironment {
    private final CategoryRepository categoryRepository;
    private final IS3Service s3Service;

    protected CategoryService(final Environment environment, final CategoryRepository categoryRepository, final IS3Service s3Service) {
        super(environment);
        this.categoryRepository = categoryRepository;
        this.s3Service = s3Service;
    }

    public List<CategoryResponse> allCategories() {
        return categoryRepository.allCategoriesStoreFront()
                .stream()
                .map(c -> CategoryResponse.builder()
                        .categoryId(c.categoryId())
                        .name(c.name())
                        .parentId(c.parentId())
                        .visible(null)
                        .build())
                .toList();
    }

    public Pageable<ProductResponse> allProductsByCategoryId(
            final CapstoneCurrency currency,
            final long categoryId,
            final int page,
            final int size
    ) {
        final Page of = Page.of(page, size);

        final int count = categoryRepository.countAllProductsByCategoryIdWhereInStockAndIsVisible(categoryId);
        final var listOfProducts = categoryRepository.allProductsByCategoryIdWhereInStockAndIsVisible(categoryId, currency, of);

        final var futures = listOfProducts.stream()
                .map(p -> (Supplier<ProductResponse>) () -> ProductResponse.builder()
                        .id(p.uuid())
                        .name(p.name())
                        .desc(p.description())
                        .price(p.price())
                        .currency(p.currency().name())
                        .imageKey(s3Service.preSignedUrl(super.awsbucket, p.imageKey()))
                        .build())
                .toList();

        final var products = CustomUtil.asynchronousTasks(futures).join();
        return new Pageable<>(of, count, products);
    }

}