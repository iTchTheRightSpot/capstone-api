package com.sarabrandserver.category.service;

import com.sarabrandserver.aws.S3Service;
import com.sarabrandserver.category.projection.CategoryPojo;
import com.sarabrandserver.category.repository.CategoryRepository;
import com.sarabrandserver.category.response.CategoryResponse;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.product.response.ProductResponse;
import com.sarabrandserver.util.CustomUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientCategoryService {

    @Value(value = "${aws.bucket}")
    private String BUCKET;

    private final CategoryRepository repository;
    private final S3Service s3Service;

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

    public Page<ProductResponse> allProductsByUUID(
            SarreCurrency currency,
            long id,
            int page,
            int size
    ) {
        return this.repository
                .productsByCategoryClient(id, currency, PageRequest.of(page, size)) //
                .map(pojo -> {
                    var url = this.s3Service.preSignedUrl(BUCKET, pojo.getKey());
                    return ProductResponse.builder()
                            .category(pojo.getCategory())
                            .id(pojo.getUuid())
                            .name(pojo.getName())
                            .desc(pojo.getDescription())
                            .price(pojo.getPrice())
                            .currency(pojo.getCurrency())
                            .imageUrl(url)
                            .build();
                });
    }

}
