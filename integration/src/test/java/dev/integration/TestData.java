package dev.integration;

import com.github.javafaker.Faker;
import dev.webserver.exception.CustomServerError;
import dev.webserver.product.CreateProductDto;
import dev.webserver.product.PriceCurrencyDto;
import dev.webserver.product.SizeInventoryDto;
import dev.webserver.product.UpdateProductDto;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.HttpCookie;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestData {

    @NotNull
    public static SizeInventoryDto[] sizeInventoryDTOArray(int size) {
        return IntStream.range(0, size)
                .mapToObj(i -> new SizeInventoryDto(new Faker().number().randomDigitNotZero() + 1, "tall " + i))
                .toArray(SizeInventoryDto[]::new);
    }

    /**
     * Just like creating a {@link MockMultipartFile} for {@link MockMvc}, this achieves the same thing
     * but for {@link WebTestClient}.
     *
     * @param dto application/json to send the server.
     * @return a {@link MultiValueMap} where the key is the parameter the server accepts.
     * @see <a href="https://github.com/spring-projects/spring-framework/issues/20666">github issue</a>
     * */
    @NotNull
    public static MultiValueMap<String, Object> mockMultiPart(String dto) {
        MultiValueMap<String, Object> multipart = new LinkedMultiValueMap<>();

        // add image files to request
        for (var resource : mockMultipartFiles()) {
            multipart.add("files", resource.getResource());
        }

        // create dto
        HttpHeaders metadataHeaders = new HttpHeaders();
        metadataHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        multipart.add("dto", new HttpEntity<>(dto, metadataHeaders));

        return multipart;
    }

    @NotNull
    private static MockMultipartFile[] mockMultipartFiles() {
        Path path = Paths.get("src/test/resources/uploads/");

        assertTrue(Files.exists(path));

        File dir = new File(path.toUri());
        assertNotNull(dir);

        File[] files = dir.listFiles();
        assertNotNull(files);

        return Arrays.stream(files).map(file -> {
                    try {
                        return new MockMultipartFile(
                                file.getName(),
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
    public static CreateProductDto createProductDTO(
            String productName,
            long categoryId,
            SizeInventoryDto[] dtos
    ) {
        return productDTO(categoryId, productName, dtos, new Faker().commerce().color());
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
    public static UpdateProductDto updateProductDTO(
            String productID,
            String productName,
            long categoryId
    ) {
        return new UpdateProductDto(
                productID,
                productName,
                new Faker().lorem().fixedString(1000),
                "ngn",
                new BigDecimal(new Faker().number().numberBetween(1000, 700000)),
                categoryId,
                new Faker().number().randomDouble(5, 100, 100)
        );
    }

    @NotNull
    public static ResponseCookie toCookie(String cookie) {
        return HttpCookie.parse(cookie)
                .stream()
                .findFirst()
                .map(httpCookie -> ResponseCookie
                        .from(httpCookie.getName(), httpCookie.getValue())
                        .domain(httpCookie.getDomain())
                        .path(httpCookie.getPath())
                        .maxAge(httpCookie.getMaxAge())
                        .httpOnly(httpCookie.getSecure())
                        .secure(httpCookie.getSecure())
                        .sameSite("lax")
                        .build()
                )
                .orElseThrow();
    }

}