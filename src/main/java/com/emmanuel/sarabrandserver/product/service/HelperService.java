package com.emmanuel.sarabrandserver.product.service;

import com.emmanuel.sarabrandserver.aws.S3Service;
import com.emmanuel.sarabrandserver.exception.CustomAwsException;
import com.emmanuel.sarabrandserver.product.entity.ProductDetail;
import com.emmanuel.sarabrandserver.product.entity.ProductImage;
import com.emmanuel.sarabrandserver.product.repository.ProductImageRepo;
import com.emmanuel.sarabrandserver.product.util.CustomMultiPart;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class HelperService {

    private final ProductImageRepo productImageRepo;
    private final S3Service s3Service;

    public String generatePreSignedUrl(boolean profile, @NotNull String bucket, @NotNull String key) {
        return this.s3Service.getPreSignedUrl(profile, bucket, key);
    }

    public void deleteFromS3(List<ObjectIdentifier> keys, String bucket) {
        this.s3Service.deleteFromS3(keys, bucket);
    }

    /**
     * Save to s3 before Create ProductImage
     *
     * @throws CustomAwsException if there is an error uploading to S3
     */
    public void productImages(ProductDetail detail, CustomMultiPart[] files, boolean profile, String bucket) {
        for (CustomMultiPart file : files) {
            var obj = new ProductImage(file.key(), file.file().getAbsolutePath(), detail);

            // Upload image to S3 if in desired profile
            if (profile) {
                this.s3Service.uploadToS3(file.file(), file.metadata(), bucket, file.key());
            }

            // Save ProductImage
            this.productImageRepo.save(obj);
        }
    }

    /**
     * Validates if items in MultipartFile array are all images, else an error is thrown.
     * Note I am returning an array as it is a bit more efficient than arraylist in terms of memory
     *
     * @param multipartFiles is an array of MultipartFile
     * @param defaultKey is Product param default key
     * @return CustomMultiPart array
     */
    public CustomMultiPart[] customMultiPartFiles(MultipartFile[] multipartFiles, StringBuilder defaultKey) {
        CustomMultiPart[] arr = new CustomMultiPart[multipartFiles.length];

        for (int i = 0; i < multipartFiles.length; i++) {
            String originalFileName = Objects.requireNonNull(multipartFiles[i].getOriginalFilename());

            File file = new File(originalFileName);

            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                // write MultipartFile to file
                outputStream.write(multipartFiles[i].getBytes());

                // Validate file is an image
                String contentType = Files.probeContentType(file.toPath());
                if (!contentType.startsWith("image/")) {
                    log.warn("File is not an image");
                    throw new CustomAwsException("File is not an image");
                }

                // Create image metadata for storing in AWS
                Map<String, String> metadata = new HashMap<>();
                metadata.put("Content-Type", contentType);
                metadata.put("Title", originalFileName);
                metadata.put("Type", StringUtils.getFilenameExtension(originalFileName));

                // Default key
                String key = UUID.randomUUID().toString();
                if (defaultKey.isEmpty()) {
                    defaultKey.append(key);
                }

                // Copy into array
                arr[i] = new CustomMultiPart(file, metadata, key);
            } catch (IOException ex) {
                log.warn("Error either writing multipart to file or getting file type. {}", ex.getMessage());
                throw new CustomAwsException("Please verify file is an image");
            }
        }
        return arr;
    }

}
