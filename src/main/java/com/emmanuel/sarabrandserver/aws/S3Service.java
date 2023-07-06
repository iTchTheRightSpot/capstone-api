package com.emmanuel.sarabrandserver.aws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service @Slf4j
public class S3Service {
    private final S3Client s3Client;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /** Upload images to s3*/
    public void uploadToS3(MultipartFile multipartFile, PutObjectRequest req) {
        File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));

        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(multipartFile.getBytes());
            this.s3Client.putObject(req, RequestBody.fromFile(file));
        } catch (S3Exception | IOException e) {
            log.error("Error deleting image from s3 {}", e.getMessage());
            throw new RuntimeException("Error deleting image. Please try again or call developer");
        }
    }

    /** Deletes images from s3 */
    public void deleteImagesFromS3(List<ObjectIdentifier> keys, String bucketName) {
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
            throw new RuntimeException("Error deleting image. Please try again or call developer");
        }
    }


}
