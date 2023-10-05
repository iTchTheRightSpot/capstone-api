package com.sarabrandserver.util;

import com.github.javafaker.Faker;
import com.sarabrandserver.enumeration.RoleEnum;
import com.sarabrandserver.product.dto.CreateProductDTO;
import com.sarabrandserver.product.dto.ProductDetailDTO;
import com.sarabrandserver.product.dto.SizeInventoryDTO;
import com.sarabrandserver.product.dto.UpdateProductDTO;
import com.sarabrandserver.user.entity.ClientRole;
import com.sarabrandserver.user.entity.SarreBrandUser;
import jakarta.validation.constraints.NotNull;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.HashSet;

public class TestingData {

    public static SarreBrandUser client() {
        var client = SarreBrandUser.builder()
                .firstname(new Faker().name().firstName())
                .lastname(new Faker().name().lastName())
                .email(new Faker().name().fullName())
                .phoneNumber(new Faker().phoneNumber().phoneNumber())
                .password(new Faker().phoneNumber().phoneNumber())
                .enabled(true)
                .clientRole(new HashSet<>())
                .build();
        client.addRole(new ClientRole(RoleEnum.CLIENT));
        return client;
    }

    public static SarreBrandUser worker() {
        var client = SarreBrandUser.builder()
                .firstname(new Faker().name().firstName())
                .lastname(new Faker().name().lastName())
                .email(new Faker().name().fullName())
                .phoneNumber(new Faker().phoneNumber().phoneNumber())
                .password(new Faker().phoneNumber().phoneNumber())
                .enabled(true)
                .clientRole(new HashSet<>())
                .build();
        client.addRole(new ClientRole(RoleEnum.CLIENT));
        client.addRole(new ClientRole(RoleEnum.WORKER));
        return client;
    }

    @NotNull
    public static SizeInventoryDTO[] sizeInventoryDTOArray(int size) {
        SizeInventoryDTO[] dto = new SizeInventoryDTO[size];

        for (int i = 0; i < size; i++) {
            dto[i] = new SizeInventoryDTO(new Faker().number().randomDigitNotZero(), "tall");
        }
        return dto;
    }

    /** Collection is an empty string */
    @NotNull
    public static Result getResult(SizeInventoryDTO[] sizeDto, String prodName, String category, String colour) {
        var dto = productDTO(
                category,
                "",
                prodName,
                sizeDto,
                colour
        );

        MockMultipartFile[] files = files(3);

        return new Result(dto, files);
    }

    /** Collection is not an empty string */
    @NotNull
    public static Result getResultCollection(
            String collection,
            SizeInventoryDTO[] dtos,
            String prodName,
            String category,
            String colour
    ) {
        var dto = productDTO(
                category,
                collection,
                prodName,
                dtos,
                colour
        );

        MockMultipartFile[] files = files(3);

        return new Result(dto, files);
    }

    @NotNull
    public static MockMultipartFile[] files(int num) {
        MockMultipartFile[] files = new MockMultipartFile[num];
        for (int i = 0; i < num; i++) {
            files[i] = new MockMultipartFile(
                    "files",
                    "uploads/image%s.jpeg".formatted(i + 1),
                    "image/jpeg",
                    "Test image content".getBytes()
            );
        }
        return files;
    }

    @NotNull
    public static CreateProductDTO createProductDTO(SizeInventoryDTO[] dtos) {
        return productDTO(
                new Faker().commerce().department(),
                new Faker().commerce().department(),
                new Faker().commerce().productName(),
                dtos,
                new Faker().commerce().color()
        );
    }

    @NotNull
    public static CreateProductDTO createProductDTOCollectionNotPresent(
            String product,
            String category,
            String collection,
            SizeInventoryDTO[] dtos
    ) {
        return productDTO(category, collection, product, dtos, new Faker().commerce().color());
    }

    @NotNull
    public static CreateProductDTO productDTO(
            String category,
            String collection,
            String productName,
            SizeInventoryDTO[] dtos,
            String colour
    ) {
        return new CreateProductDTO(
                category,
                collection,
                productName,
                new Faker().lorem().characters(0, 255),
                new BigDecimal(new Faker().number().numberBetween(20, 80)),
                "NGN",
                true,
                dtos,
                colour
        );
    }

    @NotNull
    public static ProductDetailDTO productDetailDTO(String productID, String colour, SizeInventoryDTO[] dtos) {
        return new ProductDetailDTO(productID, false, colour, dtos);
    }

    @NotNull
    public static UpdateProductDTO updateProductDTO(
            String productID,
            String productName,
            String category,
            String categoryId,
            String collection,
            String collectionId
    ) {
        return new UpdateProductDTO(
                productID,
                productName,
                new Faker().lorem().characters(0, 400),
                new BigDecimal(new Faker().number().numberBetween(20, 200)),
                category,
                categoryId,
                collection,
                collectionId
        );
    }

}
