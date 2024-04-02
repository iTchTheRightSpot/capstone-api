package dev.webserver.aws;

import dev.webserver.exception.CustomServerError;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class S3Service {

    private static final Logger log = LoggerFactory.getLogger(S3Service.class);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final boolean profile;

    public S3Service (S3Client s3Client, S3Presigner s3Presigner, Environment env) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;

        this.profile = env.getProperty("on.custom.profile", "native-test").equals("native-test")
                || env.getProperty("spring.profiles.active", "default").equals("test");
    }

    public void uploadToS3(File file, Map<String, String> metadata, String bucket, String key) {
        if (profile) {
            return;
        }
        this.uploadToS3Impl(file, metadata, bucket, key);
    }

    /**
     * Upload image to s3. As per docs
     * <a href="https://github.com/awsdocs/aws-doc-sdk-examples/blob/main/javav2/example_code/s3/src/main/java/com/example/s3/PutObject.java">...</a>
     * */
    private void uploadToS3Impl(File file, Map<String, String> metadata, String bucket, String key) {
        try {
            // Create put request
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket) // pass as env variable
                    .key(key)
                    .metadata(metadata)
                    .build();

            this.s3Client.putObject(request, RequestBody.fromFile(file));
        } catch (S3Exception e) {
            log.error("Error uploading image to s3 " + e.getMessage());
            throw new CustomServerError("error uploading image. Please try again or call developer");
        }
    }

    public void deleteFromS3(List<ObjectIdentifier> keys, String bucket) {
        if (profile) {
            return;
        }
        this.deleteFromS3Impl(keys, bucket);
    }

    /**
     * Delete image(s) from s3. As per docs
     * <a href="https://github.com/awsdocs/aws-doc-sdk-examples/blob/main/javav2/example_code/s3/src/main/java/com/example/s3/DeleteObjects.java">...</a>
     * */
    private void deleteFromS3Impl(List<ObjectIdentifier> keys, String bucketName) {
        Delete del = Delete.builder().objects(keys).build();
        try {
            DeleteObjectsRequest multiObjectDeleteRequest = DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete(del)
                    .build();
            this.s3Client.deleteObjects(multiObjectDeleteRequest);
        } catch (S3Exception e) {
            log.error("Error deleting image from s3 " + e.getMessage());
            throw new CustomServerError("Error deleting image. Please try again later or call developer");
        }
    }

    /**
     * Returns a pre-signed url from s3.
     *
     * @param bucket is the bucket name.
     * @param key is the object key.
     * @return An aws pre-signed url.
     * */
    public String preSignedUrl(@NotNull String bucket, @NotNull String key) {
        if (profile) {
            return "";
        }
        return preSignedUrlImpl(bucket, key);
    }

    /**
     * Retrieves image pre-assigned url. As per docs
     * <a href="https://github.com/awsdocs/aws-doc-sdk-examples/blob/main/javav2/example_code/s3/src/main/java/com/example/s3/GetObjectPresignedUrl.java">...</a>
     *
     * @param bucket is the bucket name
     * @param key is the object key
     * @return String
     * */
    private String preSignedUrlImpl(String bucket, String key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(30))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest request = this.s3Presigner.presignGetObject(getObjectPresignRequest);
            log.info("successfully retrieved object preassigned URL");
            return request.url().toString();
        } catch (S3Exception ex) {
            log.error("error retrieving object preassigned url " + ex.getMessage());
            return "";
        }
    }

}