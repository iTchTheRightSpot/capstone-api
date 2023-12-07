package com.sarabrandserver.aws;

import com.sarabrandserver.exception.CustomAwsException;
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

    private static final Logger log = LoggerFactory.getLogger(S3Service.class.getName());
    private static boolean PROFILE;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    public S3Service(Environment env, S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;

        String activeProfile = env.getProperty("spring.profiles.active", "");
        PROFILE = activeProfile.equals("test");
    }

    public void uploadToS3(File file, Map<String, String> metadata, String bucket, String key) {
        if (PROFILE) {
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
            throw new CustomAwsException("Error uploading image. Please try again or call developer");
        }
    }

    public void deleteFromS3(List<ObjectIdentifier> keys, String bucket) {
        if (PROFILE) {
            return;
        }
        this.deleteFromS3Impl(keys, bucket);
    }

    /**
     * Delete image(s) from s3. As per docs
     * <a href="https://github.com/awsdocs/aws-doc-sdk-examples/blob/main/javav2/example_code/s3/src/main/java/com/example/s3/DeleteObjects.java">...</a>
     * */
    public void deleteFromS3Impl(List<ObjectIdentifier> keys, String bucketName) {
        Delete del = Delete.builder()
                .objects(keys)
                .build();
        try {
            DeleteObjectsRequest multiObjectDeleteRequest = DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete(del)
                    .build();
            this.s3Client.deleteObjects(multiObjectDeleteRequest);
        } catch (S3Exception e) {
            log.error("Error deleting image from s3 " + e.getMessage());
            throw new CustomAwsException("Error deleting image. Please try again later or call developer");
        }
    }

    /**
     * Returns a pre-signed url from s3
     *
     * @param bucket is the bucket name
     * @param key is the object key
     * @return String
     * */
    public String getPreSignedUrl(@NotNull String bucket, @NotNull String key) {
        if (PROFILE) {
            return "";
        }
        return getPreSignedUrlImpl(bucket, key);
    }

    /**
     * Retrieves image pre-assigned url. As per docs
     * <a href="https://github.com/awsdocs/aws-doc-sdk-examples/blob/main/javav2/example_code/s3/src/main/java/com/example/s3/GetObjectPresignedUrl.java">...</a>
     *
     * @param bucket is the bucket name
     * @param key is the object key
     * @return String
     * */
    private String getPreSignedUrlImpl(String bucket, String key) {
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
            log.info("Successfully retrieved object preassigned URL");
            return request.url().toString();
        } catch (S3Exception ex) {
            log.error("Error retrieving object preassigned url " + ex.getMessage());
            return "";
        }
    }

}
