package com.emmanuel.sarabrandserver.product;

import com.emmanuel.sarabrandserver.product.util.CreateProductDTO;
import com.emmanuel.sarabrandserver.product.util.SizeInventoryDTO;
import com.github.javafaker.Faker;
import jakarta.validation.constraints.NotNull;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;

public class ProductTestingData {

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

        MockMultipartFile[] files = {
                new MockMultipartFile(
                        "file",
                        "uploads/image1.jpeg",
                        "image/jpeg",
                        "Test image content".getBytes()
                ),
                new MockMultipartFile(
                        "file",
                        "uploads/image2.jpeg",
                        "image/jpeg",
                        "Test image content".getBytes()
                ),
                new MockMultipartFile(
                        "file",
                        "uploads/image3.jpeg",
                        "image/jpeg",
                        "Test image content".getBytes()
                ),
        };
        return new Result(dto, files);
    }

}
