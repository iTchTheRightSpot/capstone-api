package dev.integration;

import com.github.javafaker.Faker;
import dev.webserver.product.dto.*;
import dev.webserver.user.entity.ClientRole;
import dev.webserver.user.entity.SarreBrandUser;
import jakarta.validation.constraints.NotNull;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;

import static dev.webserver.enumeration.RoleEnum.CLIENT;
import static dev.webserver.enumeration.RoleEnum.WORKER;

public class TestData {
    public static SarreBrandUser client() {
        var client = SarreBrandUser.builder()
                .firstname(new Faker().name().firstName())
                .lastname(new Faker().name().lastName())
                .email(new Faker().name().fullName())
                .phoneNumber(new Faker().phoneNumber().phoneNumber())
                .password(new Faker().phoneNumber().phoneNumber())
                .enabled(true)
                .build();
        client.setClientRole(Set.of(new ClientRole(CLIENT, client)));
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
                .build();
        client.setClientRole(
                Set.of(new ClientRole(CLIENT, client), new ClientRole(WORKER, client))
        );
        return client;
    }

    @NotNull
    public static SizeInventoryDTO[] sizeInventoryDTOArray(int size) {
        SizeInventoryDTO[] dto = new SizeInventoryDTO[size];
        for (int i = 0; i < size; i++) {
            dto[i] = new SizeInventoryDTO(new Faker().number().randomDigitNotZero() + 1, "tall " + i);
        }
        return dto;
    }

    /**
     * Converts all files from uploads directory into a MockMultipartFile
     */
    @NotNull
    public static MockMultipartFile[] files() {
        return Arrays.stream(new Path[]{Paths.get("src/test/resources/uploads/benzema.JPG")})
                .map(path -> {
                    String contentType;
                    byte[] content;
                    try {
                        contentType = Files.probeContentType(path);
                        content = Files.readAllBytes(path);
                    } catch (IOException ignored) {
                        contentType = "text/plain";
                        content = new byte[3];
                    }

                    return new MockMultipartFile(
                            "files",
                            path.getFileName().toString(),
                            contentType,
                            content
                    );
                })
                .toArray(MockMultipartFile[]::new);
    }

    @NotNull
    public static CreateProductDTO createProductDTO(long categoryId, SizeInventoryDTO[] dtos) {
        return productDTO(
                categoryId,
                new Faker().commerce().productName(),
                dtos,
                new Faker().commerce().color()
        );
    }

    @NotNull
    public static CreateProductDTO createProductDTO(
            String productName,
            long categoryId,
            SizeInventoryDTO[] dtos
    ) {
        return productDTO(categoryId, productName, dtos, new Faker().commerce().color());
    }

    @NotNull
    public static CreateProductDTO productDTOWeight(
            long categoryId,
            String productName,
            SizeInventoryDTO[] dtos,
            PriceCurrencyDto[] pcDto,
            String colour,
            double weight
    ) {
        return new CreateProductDTO(
                categoryId,
                productName,
                new Faker().lorem().fixedString(1000),
                weight,
                pcDto,
                true,
                dtos,
                colour
        );
    }

    @NotNull
    public static CreateProductDTO productDTO(
            long categoryId,
            String productName,
            SizeInventoryDTO[] dtos,
            String colour
    ) {
        PriceCurrencyDto[] arr = {
                new PriceCurrencyDto(new BigDecimal(new Faker().commerce().price()), "USD"),
                new PriceCurrencyDto(new BigDecimal(new Faker().number().numberBetween(10000, 700000)), "NGN"),
        };

        return new CreateProductDTO(
                categoryId,
                productName,
                new Faker().lorem().fixedString(1000),
                new Faker().number().randomDouble(5, 0, 50),
                arr,
                true,
                dtos,
                colour
        );
    }

    @NotNull
    public static ProductDetailDto productDetailDTO(String productID, String colour, SizeInventoryDTO[] dtos) {
        return new ProductDetailDto(productID, false, colour, dtos);
    }

    @NotNull
    public static UpdateProductDTO updateProductDTO(
            String productID,
            String productName,
            long categoryId
    ) {
        return new UpdateProductDTO(
                productID,
                productName,
                new Faker().lorem().fixedString(1000),
                "ngn",
                new BigDecimal(new Faker().number().numberBetween(1000, 700000)),
                categoryId,
                new Faker().number().randomDouble(5, 100, 100)
        );
    }

}
