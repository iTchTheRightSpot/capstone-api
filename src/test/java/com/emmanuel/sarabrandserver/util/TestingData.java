package com.emmanuel.sarabrandserver.util;

import com.emmanuel.sarabrandserver.enumeration.RoleEnum;
import com.emmanuel.sarabrandserver.product.util.CreateProductDTO;
import com.emmanuel.sarabrandserver.product.util.SizeInventoryDTO;
import com.emmanuel.sarabrandserver.product.util.UpdateProductDTO;
import com.emmanuel.sarabrandserver.user.entity.ClientRole;
import com.emmanuel.sarabrandserver.user.entity.SarreBrandUser;
import com.github.javafaker.Faker;
import jakarta.validation.constraints.NotNull;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

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
            var obj = SizeInventoryDTO.builder()
                    .size("tall") // prevent duplicate
                    .qty(new Faker().number().randomDigitNotZero())
                    .build();
            dto[i] = obj;
        }
        return dto;
    }

    @NotNull
    public static Result getResult(SizeInventoryDTO[] sizeDto, String prodName, String cat, String colour) {
        var dto = CreateProductDTO.builder()
                .category(cat)
                .collection("")
                .name(prodName)
                .desc(new Faker().lorem().characters(255))
                .price(new BigDecimal(new Faker().commerce().price()))
                .currency("USD")
                .sizeInventory(sizeDto)
                .visible(true)
                .colour(colour)
                .build();

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
    public static CreateProductDTO createProductDTO(SizeInventoryDTO[] dtos, MultipartFile[] files) {
        return CreateProductDTO.builder()
                .sizeInventory(dtos)
                .category(new Faker().commerce().department())
                .collection(new Faker().commerce().department())
                .name(new Faker().commerce().productName())
                .desc(new Faker().lorem().characters(0, 255))
                .price(new BigDecimal(new Faker().number().numberBetween(20, 80)))
                .currency("NGN")
                .visible(true)
                .colour(new Faker().commerce().color())
                .files(files)
                .build();
    }

    @NotNull
    public static UpdateProductDTO updateProductDTO (String collection, String collectionId) {
        return UpdateProductDTO.builder()
                .category(new Faker().commerce().department())
                .categoryId("category id")
                .collection(collection)
                .collectionId(collectionId)
                .uuid("product id")
                .name("product name")
                .desc(new Faker().lorem().characters(0, 400))
                .price(new BigDecimal(new Faker().number().numberBetween(20, 200)))
                .build();
    }

}
