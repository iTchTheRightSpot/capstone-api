package dev.webserver.external.aws;

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
class S3ServiceImpl implements IS3Service {

    private static final Logger log = LoggerFactory.getLogger(S3ServiceImpl.class);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final boolean profile;

    public S3ServiceImpl(S3Client s3Client, S3Presigner s3Presigner, Environment env) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;

        String active = env.getProperty("spring.profiles.active", "default");

        profile = active.endsWith("test");
    }

    @Override
    public void uploadToS3(File file, Map<String, String> metadata, String bucket, String key) {
        if (profile) {
            return;
        }
        this.uploadToS3Impl(file, metadata, bucket, key);
    }

    /**
     * Upload file to s3.
     * @see <a href="https://github.com/awsdocs/aws-doc-sdk-examples/blob/main/javav2/example_code/s3/src/main/java/com/example/s3/PutObject.java">aws docs</a>
     * */
    private void uploadToS3Impl(File file, Map<String, String> metadata, String bucket, String key) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .metadata(metadata)
                .build();
        try {
            this.s3Client.putObject(request, RequestBody.fromFile(file));
            log.info("successfully uploaded file to s3 {}", file.getName());
        } catch (Exception e) {
            log.error("Error uploading image to s3 {}", e.getMessage());
            throw new CustomServerError("an error occurred uploading image. Please try again or contact developer");
        }
    }

    public void deleteFromS3(List<ObjectIdentifier> keys, String bucket) {
        if (profile) {
            return;
        }
        this.deleteFromS3Impl(keys, bucket);
    }

    /**
     * Delete image(s) from s3.
     * @see <a href="https://github.com/awsdocs/aws-doc-sdk-examples/blob/main/javav2/example_code/s3/src/main/java/com/example/s3/DeleteObjects.java">aws docs</a>
     * */
    private void deleteFromS3Impl(List<ObjectIdentifier> keys, String bucketName) {
        DeleteObjectsRequest build = DeleteObjectsRequest.builder()
                .bucket(bucketName)
                .delete(Delete.builder().objects(keys).build())
                .build();
        try {
            s3Client.deleteObjects(build);
            log.info("successfully deleted files from s3");
        } catch (S3Exception e) {
            log.error("error deleting image from s3 {}", e.getMessage());
            throw new CustomServerError("an error occurred deleting image(s). Please try again later or contact developer");
        }
    }

    @Override
    public String preSignedUrl(@NotNull String bucket, @NotNull String key) {
        if (profile) {
            return "";
        }
        return preSignedUrlImpl(bucket, key);
    }

    /**
     * Retrieves image pre-assigned url.
     * @see <a href="https://github.com/awsdocs/aws-doc-sdk-examples/blob/main/javav2/example_code/s3/src/main/java/com/example/s3/GetObjectPresignedUrl.java">aws docs</a>
     *
     * @param bucket is the bucket name
     * @param key is the object key
     * @return an uploaded {@link File} as aws pre-signed url.
     * */
    private String preSignedUrlImpl(String bucket, String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(30))
                .getObjectRequest(getObjectRequest)
                .build();
        try {
            PresignedGetObjectRequest request = this.s3Presigner.presignGetObject(getObjectPresignRequest);
            log.info("successfully retrieved object preassigned URL");
            return request.url().toString();
        } catch (S3Exception ex) {
            log.error("error retrieving object preassigned url {}", ex.getMessage());
            return "";
        }
    }

}