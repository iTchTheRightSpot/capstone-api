package com.sarabrandserver.product.service;

import com.sarabrandserver.aws.S3Service;
import com.sarabrandserver.exception.CustomAwsException;
import com.sarabrandserver.product.entity.ProductDetail;
import com.sarabrandserver.product.entity.ProductImage;
import com.sarabrandserver.product.repository.ProductImageRepo;
import com.sarabrandserver.product.response.CustomMultiPart;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class HelperService {

    private static final Logger log = LoggerFactory.getLogger(HelperService.class);

    private final ProductImageRepo productImageRepo;
    private final S3Service s3Service;

    public String preSignedURL(@NotNull String bucket, @NotNull String key) {
        return this.s3Service.preSignedUrl(bucket, key);
    }

    public void deleteFromS3(List<ObjectIdentifier> keys, String bucket) {
        this.s3Service.deleteFromS3(keys, bucket);
    }

    /**
     * Save to s3 before Create ProductImage
     *
     * @throws CustomAwsException if there is an error uploading to S3
     */
    public void productImages(ProductDetail detail, CustomMultiPart[] files, String bucket) {
        for (CustomMultiPart file : files) {
            var obj = new ProductImage(file.key(), file.file().getAbsolutePath(), detail);

            // Upload image to S3 if in desired profile
            this.s3Service.uploadToS3(file.file(), file.metadata(), bucket, file.key());

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
        return Arrays.stream(multipartFiles)
                .map(multipartFile -> {
                    String originalFileName = Objects.requireNonNull(multipartFile.getOriginalFilename());

                    File file = new File(originalFileName);

                    try (FileOutputStream stream = new FileOutputStream(file)) {
                        // write MultipartFile to file
                        stream.write(multipartFile.getBytes());

                        // Validate file is an image
                        String contentType = Files.probeContentType(file.toPath());
                        if (!contentType.startsWith("image/")) {
                            log.error("File is not an image");
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

                        CustomMultiPart result = new CustomMultiPart(file, metadata, key);

                        stream.close();
                        // prevents spring from saving files to root folder
                        file.delete();

                        return result;
                    } catch (IOException ex) {
                        log.error("Error either writing multipart to file or getting file type. {}", ex.getMessage());
                        throw new CustomAwsException("please verify files are images");
                    }
                }) //
                .toArray(CustomMultiPart[]::new);
    }

}
