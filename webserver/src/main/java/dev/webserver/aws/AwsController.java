package dev.webserver.aws;

import dev.webserver.product.repository.ProductImageRepo;
import dev.webserver.product.repository.ProductRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(path = "${api.endpoint.baseurl}native")
@RequiredArgsConstructor
class AwsController {

    private static final Logger log = LoggerFactory.getLogger(AwsController.class);

    @Value(value = "${spring.profiles.active}")
    private String profile;
    @Value(value = "${aws.bucket}")
    private String bucket;

    private final ProductRepo productRepo;
    private final ProductImageRepo imageRepo;
    private final S3Service s3Service;

    @GetMapping
    @ResponseStatus(OK)
    public String testControl() {
        log.info("Aws Controller current profile {}", profile);

        if (!profile.equals("native-test"))
            return "cant access route. invalid profile";

        final var productKeys = productRepo.findAll().stream()
                .map(p -> ObjectIdentifier.builder().key(p.getDefaultKey()).build())
                .toList();

        s3Service.deleteFromS3(productKeys, bucket);

        final var imageKeys = imageRepo.findAll().stream()
                .map(i -> ObjectIdentifier.builder().key(i.getImageKey()).build())
                .toList();

        s3Service.deleteFromS3(imageKeys, bucket);
        return "deleted";
    }

    private static class CustomAuthException extends AuthenticationException {
        public CustomAuthException(String msg) {
            super(msg);
        }
    }
}
