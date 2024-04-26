package dev.webserver.aws;

import dev.webserver.product.repository.ProductImageRepo;
import dev.webserver.product.repository.ProductRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping(path = "${api.endpoint.baseurl}native")
@PreAuthorize(value = "hasRole('ROLE_NATIVE_TEST')")
@RequiredArgsConstructor
class AwsController {

    @Value(value = "${spring.profiles.active}")
    private String profile;
    @Value(value = "${aws.bucket}")
    private String bucket;

    private final ProductRepo productRepo;
    private final ProductImageRepo imageRepo;
    private final S3Service s3Service;

    @PostMapping
    @ResponseStatus(CREATED)
    void test() {
        if (!profile.equals("native-test"))
            throw new CustomAuthException("Last warning else will block your ip.");

        final var productKeys = productRepo.findAll().stream()
                .map(p -> ObjectIdentifier.builder().key(p.getDefaultKey()).build())
                .toList();

        s3Service.deleteFromS3(productKeys, bucket);

        final var imageKeys = imageRepo.findAll().stream()
                .map(i -> ObjectIdentifier.builder().key(i.getImageKey()).build())
                .toList();

        s3Service.deleteFromS3(imageKeys, bucket);
    }

    private static class CustomAuthException extends AuthenticationException {
        public CustomAuthException(String msg) {
            super(msg);
        }
    }
}
