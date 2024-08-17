package dev.webserver.category;

import dev.webserver.AbstractEnvironment;
import dev.webserver.cache.CacheImpl;
import dev.webserver.cache.CacheEnum;
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

import static dev.webserver.cache.CacheEnum.STORE_FRONT;

@Service
class CategoryService extends AbstractEnvironment {
    private final CategoryRepository repository;
    private final IS3Service s3Service;
    private final CacheImpl<CacheEnum, List<CategoryResponse>> allCategoriesCache;
    private final CacheImpl<String, Pageable<ProductResponse>> productResponsePageableCache;

    protected CategoryService(final Environment environment, final CategoryRepository repository, final IS3Service s3Service, final CacheImpl<CacheEnum, List<CategoryResponse>> allCategoriesCache, final CacheImpl<String, Pageable<ProductResponse>> productResponsePageableCache) {
        super(environment);
        this.repository = repository;
        this.s3Service = s3Service;
        this.allCategoriesCache = allCategoriesCache;
        this.productResponsePageableCache = productResponsePageableCache;
    }

    public List<CategoryResponse> allCategories() {
        final var optional = allCategoriesCache.getIfPresent(STORE_FRONT);

        if (optional.isPresent()) return optional.get();

        final var list = repository.allCategoriesStoreFront()
                .stream()
                .map(c -> CategoryResponse.builder()
                        .categoryId(c.categoryId())
                        .name(c.name())
                        .parentId(c.parentId())
                        .visible(null)
                        .build())
                .toList();

        allCategoriesCache.put(STORE_FRONT, list);

        return list;
    }

    public Pageable<ProductResponse> allProductsByCategoryId(
            final CapstoneCurrency currency,
            final long categoryId,
            final int page,
            final int size
    ) {
        final String key = String.format("%s_%s_%d_%d_%d", STORE_FRONT, currency, categoryId, page, size);
        final var cache = productResponsePageableCache.getIfPresent(key);

        if (cache.isPresent()) return cache.get();

        final Page of = Page.of(page, size);

        final int count = repository.countAllProductsByCategoryIdWhereInStockAndIsVisible(categoryId);
        final var listOfProducts = repository.allProductsByCategoryIdWhereInStockAndIsVisible(categoryId, currency, of);

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

        final var response = new Pageable<>(of, count, CustomUtil.asynchronousTasks(futures).join());

        productResponsePageableCache.put(key, response);

        return response;
    }

}