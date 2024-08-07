package dev.webserver.data;

import com.github.javafaker.Faker;
import dev.webserver.category.Category;
import dev.webserver.exception.CustomServerError;
import dev.webserver.product.*;
import dev.webserver.product.WorkerProductService;
import dev.webserver.user.ClientRole;
import dev.webserver.user.SarreBrandUser;
import jakarta.validation.constraints.NotNull;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;

import static dev.webserver.enumeration.RoleEnum.CLIENT;
import static dev.webserver.enumeration.RoleEnum.WORKER;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    public static SizeInventoryDto[] sizeInventoryDTOArray(int size) {
        SizeInventoryDto[] dto = new SizeInventoryDto[size];
        for (int i = 0; i < size; i++) {
            dto[i] = new SizeInventoryDto(new Faker().number().randomDigitNotZero() + 1, "tall " + i);
        }
        return dto;
    }

    /**
     * Converts all files from uploads directory into a {@link MockMultipartFile}.
     * */
    @NotNull
    public static MockMultipartFile[] files() {
        Path path = Paths.get("src/test/resources/uploads/");

        assertTrue(Files.exists(path));

        File dir = new File(path.toUri());
        assertNotNull(dir);

        File[] files = dir.listFiles();
        assertNotNull(files);

        return Arrays.stream(files).map(file -> {
                    try {
                        return new MockMultipartFile(
                                "files",
                                file.getName(),
                                Files.probeContentType(file.toPath()),
                                Files.readAllBytes(file.toPath())
                        );
                    } catch (IOException ignored) {
                        throw new CustomServerError("unable to convert files in %s to a file".formatted(path.toString()));
                    }
                })
                .toArray(MockMultipartFile[]::new);
    }

    @NotNull
    public static CreateProductDto createProductDTO(long categoryId, SizeInventoryDto[] dtos) {
        return productDTO(
                categoryId,
                new Faker().commerce().productName(),
                dtos,
                new Faker().commerce().color()
        );
    }

    @NotNull
    public static CreateProductDto createProductDTO(
            String productName,
            long categoryId,
            SizeInventoryDto[] dtos
    ) {
        return productDTO(categoryId, productName, dtos, new Faker().commerce().color());
    }

    @NotNull
    public static CreateProductDto productDTOWeight(
            long categoryId,
            String productName,
            SizeInventoryDto[] dtos,
            PriceCurrencyDto[] pcDto,
            String colour,
            double weight
    ) {
        return new CreateProductDto(
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
    public static CreateProductDto productDTO(
            long categoryId,
            String productName,
            SizeInventoryDto[] dtos,
            String colour
    ) {
        PriceCurrencyDto[] arr = {
                new PriceCurrencyDto(new BigDecimal(new Faker().commerce().price()), "USD"),
                new PriceCurrencyDto(new BigDecimal(new Faker().number().numberBetween(10000, 700000)), "NGN"),
        };

        return new CreateProductDto(
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
    public static ProductDetailDto productDetailDTO(String productID, String colour, SizeInventoryDto[] dtos) {
        return new ProductDetailDto(productID, false, colour, dtos);
    }

    @NotNull
    public static UpdateProductDto updateProductDTO(
            String productId,
            String productName,
            long categoryId
    ) {
        return new UpdateProductDto(
                productId,
                productName,
                new Faker().lorem().fixedString(1000),
                "ngn",
                new BigDecimal(new Faker().number().numberBetween(1000, 700000)),
                categoryId,
                new Faker().number().randomDouble(5, 100, 100)
        );
    }

    @NotNull
    public static void dummyProducts(Category cat, int num, WorkerProductService service) {
        var images = TestData.files();

        for (int i = 0; i < num; i++) {
            var data = TestData
                    .productDTO(
                            cat.getCategoryId(),
                            new Faker().commerce().productName() + " " + i,
                            new SizeInventoryDto[]{
                                    new SizeInventoryDto(new Faker().number().numberBetween(1, 40), "medium"),
                                    new SizeInventoryDto(new Faker().number().numberBetween(1, 40), "small"),
                                    new SizeInventoryDto(new Faker().number().numberBetween(1, 40), "large")
                            },
                            new Faker().commerce().color() + " " + i
                    );

            service.create(data, images);
        }
    }

    @NotNull
    public static void dummyProductsTestTotalAmount(
            Category cat,
            PriceCurrencyDto[] arr,
            int numOfProducts,
            int variantQty,
            double weight,
            WorkerProductService service
    ) {
        var images = TestData.files();

        for (int i = 0; i < numOfProducts; i++) {
            var data = TestData
                    .productDTOWeight(
                            cat.getCategoryId(),
                            new Faker().commerce().productName() + " " + i,
                            new SizeInventoryDto[]{ new SizeInventoryDto(variantQty, "medium") },
                            arr,
                            new Faker().commerce().color() + " " + i,
                            weight
                    );

            service.create(data, images);
        }
    }

}