package com.sarabrandserver.data;

import com.github.javafaker.Faker;
import com.sarabrandserver.product.dto.*;
import com.sarabrandserver.user.entity.ClientRole;
import com.sarabrandserver.user.entity.SarreBrandUser;
import jakarta.validation.constraints.NotNull;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;

import static com.sarabrandserver.enumeration.RoleEnum.CLIENT;
import static com.sarabrandserver.enumeration.RoleEnum.WORKER;

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
        client.addRole(new ClientRole(CLIENT));
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
        client.addRole(new ClientRole(CLIENT));
        client.addRole(new ClientRole(WORKER));
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
            String productName,
            String category,
            String collection,
            SizeInventoryDTO[] dtos
    ) {
        return productDTO(category, collection, productName, dtos, new Faker().commerce().color());
    }

    @NotNull
    public static CreateProductDTO productDTO(
            String category,
            String collection,
            String productName,
            SizeInventoryDTO[] dtos,
            String colour
    ) {
        PriceCurrencyDTO[] arr = {
                new PriceCurrencyDTO(new BigDecimal(new Faker().commerce().price()), "USD"),
                new PriceCurrencyDTO(new BigDecimal(new Faker().number().numberBetween(10000, 700000)), "NGN"),
        };

        return new CreateProductDTO(
                category,
                collection,
                productName,
                new Faker().lorem().characters(0, 255),
                arr,
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
                "ngn",
                new BigDecimal(new Faker().number().numberBetween(1000, 700000)),
                category,
                categoryId,
                collection,
                collectionId
        );
    }

}
