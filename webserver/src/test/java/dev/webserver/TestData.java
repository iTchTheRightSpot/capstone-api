package dev.webserver;

import com.github.javafaker.Faker;
import dev.webserver.category.Category;
import dev.webserver.exception.CustomServerException;
import dev.webserver.product.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class TestData {

    @NotNull
    public static SizeInventoryDto[] sizeInventoryDTOArray(final int size) {
        final SizeInventoryDto[] dto = new SizeInventoryDto[size];
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
        final Path path = Paths.get("src/test/resources/uploads/");

        assertTrue(Files.exists(path));

        final File dir = new File(path.toUri());
        assertNotNull(dir);

        final File[] files = dir.listFiles();
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
                        throw new CustomServerException("unable to convert files in %s to a file".formatted(path.toString()));
                    }
                })
                .toArray(MockMultipartFile[]::new);
    }

    @NotNull
    public static CreateProductDto createProductDTO(final long categoryId, final SizeInventoryDto[] dtos) {
        return productDTO(
                categoryId,
                new Faker().commerce().productName(),
                dtos,
                new Faker().commerce().color()
        );
    }

    @NotNull
    public static CreateProductDto createProductDTO(
            final String productName,
            final long categoryId,
            final SizeInventoryDto[] dtos
    ) {
        return productDTO(categoryId, productName, dtos, new Faker().commerce().color());
    }

    @NotNull
    public static CreateProductDto productDTOWeight(
            final long categoryId,
            final String productName,
            final SizeInventoryDto[] dtos,
            final PriceCurrencyDto[] pcDto,
            final String colour,
            final BigDecimal weight
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
            final long categoryId,
            final String productName,
            final SizeInventoryDto[] dtos,
            final String colour
    ) {
        final PriceCurrencyDto[] arr = {
                new PriceCurrencyDto(new BigDecimal(new Faker().commerce().price()), "USD"),
                new PriceCurrencyDto(new BigDecimal(new Faker().number().numberBetween(10000, 700000)), "NGN"),
        };

        return new CreateProductDto(
                categoryId,
                productName,
                new Faker().lorem().fixedString(1000),
                BigDecimal.valueOf(new Faker().number().randomDouble(5, 0, 50)),
                arr,
                true,
                dtos,
                colour
        );
    }

    @NotNull
    public static ProductDetailDto productDetailDTO(final String productID, final String colour, final SizeInventoryDto[] dtos) {
        return new ProductDetailDto(productID, false, colour, dtos);
    }

    @NotNull
    public static UpdateProductDto updateProductDTO(
            final String productId,
            final String productName,
            final long categoryId
    ) {
        return new UpdateProductDto(
                productId,
                productName,
                new Faker().lorem().fixedString(1000),
                "ngn",
                new BigDecimal(new Faker().number().numberBetween(1000, 700000)),
                categoryId,
                BigDecimal.valueOf(new Faker().number().randomDouble(5, 100, 100))
        );
    }

    @NotNull
    public static void dummyProducts(final Category cat, final int num, final EmployeeProductService service) {
        final var images = TestData.files();

        for (int i = 0; i < num; i++) {
            final var data = TestData
                    .productDTO(
                            cat.categoryId(),
                            new Faker().commerce().productName() + " " + i,
                            new SizeInventoryDto[]{
                                    new SizeInventoryDto(new Faker().number().numberBetween(20, 40), "medium"),
                                    new SizeInventoryDto(new Faker().number().numberBetween(20, 40), "small"),
                                    new SizeInventoryDto(new Faker().number().numberBetween(20, 40), "large")
                            },
                            new Faker().commerce().color() + " " + i
                    );

            service.create(data, images);
        }
    }

    @NotNull
    public static void dummyProductsTestTotalAmount(
            final Category cat,
            final PriceCurrencyDto[] arr,
            final int numOfProducts,
            final int variantQty,
            final BigDecimal weight,
            final EmployeeProductService service
    ) {
        final var images = TestData.files();

        for (int i = 0; i < numOfProducts; i++) {
            final var data = TestData
                    .productDTOWeight(
                            cat.categoryId(),
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