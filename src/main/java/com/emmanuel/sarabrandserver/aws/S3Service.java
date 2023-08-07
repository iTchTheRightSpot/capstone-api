package com.emmanuel.sarabrandserver.aws;

import com.emmanuel.sarabrandserver.exception.CustomAwsException;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

@Service @Slf4j
public class S3Service {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    public S3Service(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    /** Upload image to s3 */
    public void uploadToS3(MultipartFile multipartFile, PutObjectRequest req) {
        File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));

        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(multipartFile.getBytes());
            this.s3Client.putObject(req, RequestBody.fromFile(file));
        } catch (S3Exception e) {
            log.error("Error uploading image to s3 {}", e.getMessage());
            throw new CustomAwsException("Error uploading image. Please try again or call developer");
        } catch (IOException e) {
            log.error("Error uploading image. Image might not be a file {}", e.getMessage());
            throw new CustomAwsException("Please verify file is an image");
        }
    }

    /** Delete images from s3 */
    public void deleteFromS3(List<ObjectIdentifier> keys, String bucketName) {
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
            log.error("Error deleting image from s3 {}", e.getMessage());
            throw new CustomAwsException("Error deleting image. Please try again later or call developer");
        }
    }

    /**
     * Returns a pre-signed url from s3
     * @param profile is to verify what profile spring app running on
     * @param bucket is the bucket name
     * @param key is the object key
     * @return String
     * */
    public String getPreSignedUrl(boolean profile, @NotNull String bucket, @NotNull String key) {
        if (!profile) {
            return "";
        }
        return getPreSignedUrlImpl(bucket, key);
    }

    /**
     * Logic to return a pre-signed url from s3
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
            log.error("Error retrieving object preassigned url {}", ex.getMessage());
            return "";
        }
    }

}
