package com.sarabrandserver.data;

import com.github.javafaker.Faker;
import com.sarabrandserver.address.AddressDTO;
import com.sarabrandserver.enumeration.SarreCurrency;
import com.sarabrandserver.order.dto.PaymentDTO;
import com.sarabrandserver.order.dto.SkuQtyDTO;
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


//    public static MockMultipartFile[] files() {
//        try (Stream<Path> files = Files.list(Paths.get("src/test/resources/uploads/"))) {
//            return files.map(path -> {
//                        File file = path.toFile();
//                        try {
//                            return new MockMultipartFile(
//                                    "files",
//                                    file.getName(),
//                                    Files.probeContentType(file.toPath()),
//                                    IOUtils.toByteArray(new FileInputStream(file))
//                            );
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
//                    })
//                    .toArray(MockMultipartFile[]::new);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    /**
     * Converts all files from uploads directory into a MockMultipartFile
     * */
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
                new Faker().lorem().fixedString(1000),
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
                new Faker().lorem().fixedString(1000),
                "ngn",
                new BigDecimal(new Faker().number().numberBetween(1000, 700000)),
                category,
                categoryId,
                collection,
                collectionId
        );
    }

    @NotNull
    public static PaymentDTO paymentDTO(String email, SarreCurrency currency, SkuQtyDTO[] arr) {
        return new PaymentDTO(
                email,
                new Faker().name().fullName(),
                new Faker().phoneNumber().phoneNumber(),
                currency.name(),
                new BigDecimal(new Faker().number().numberBetween(100, 500000)),
                "Flutterwave",
                arr,
                addressDTO()
        );
    }

    @NotNull
    public static AddressDTO addressDTO() {
        return new AddressDTO(
                new Faker().address().fullAddress(),
                new Faker().address().city(),
                new Faker().address().state(),
                new Faker().address().zipCode(),
                new Faker().address().country(),
                new Faker().lorem().fixedString(1000)
        );
    }

}
