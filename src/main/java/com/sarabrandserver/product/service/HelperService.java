package com.sarabrandserver.product.service;

import com.sarabrandserver.aws.S3Service;
import com.sarabrandserver.exception.CustomAwsException;
import com.sarabrandserver.exception.CustomServerError;
import com.sarabrandserver.product.entity.Product;
import com.sarabrandserver.product.entity.ProductDetail;
import com.sarabrandserver.product.entity.ProductImage;
import com.sarabrandserver.product.repository.ProductImageRepo;
import com.sarabrandserver.product.response.CustomMultiPart;
import com.sarabrandserver.util.CustomUtil;
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
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
class HelperService {

    private static final Logger log = LoggerFactory.getLogger(HelperService.class);

    private final ProductImageRepo repository;
    private final S3Service service;

    public String preSignedUrl(@NotNull String bucket, @NotNull String key) {
        return this.service.preSignedUrl(bucket, key);
    }

    public void deleteFromS3(List<ObjectIdentifier> keys, String bucket) {
        this.service.deleteFromS3(keys, bucket);
    }

    /**
     * Concurrently uploads multiple product images to Amazon S3 and
     * saves their details to the database. This method leverages
     * multithreading by creating multiple callables, each responsible
     * for uploading and saving one image.
     *
     * @param detail The {@link ProductDetail} associated with the images.
     * @param files An array of {@link CustomMultiPart} objects representing
     *              the images to be uploaded.
     * @param bucket The name of the Amazon S3 bucket to which the images will
     *               be uploaded.
     * @throws CustomAwsException if there is an error uploading the images to
     * Amazon S3.
     * @throws CustomServerError if there is an error executing the tasks.
     */
    public void saveProductImages(ProductDetail detail, CustomMultiPart[] files, String bucket) {
        var future = Arrays.stream(files)
                .map(file -> (Supplier<CustomMultiPart>) () -> {
                    service.uploadToS3(file.file(), file.metadata(), bucket, file.key());
                    return file;
                })
                .toList();

        // save all images as long as we have successfully saved to s3
        CustomUtil.asynchronousTasks(future, HelperService.class)
                .join()
                .forEach(e -> {
                    CustomMultiPart obj = e.get();
                    this.repository
                            .save(new ProductImage(obj.key(), obj.file().getAbsolutePath(), detail));
                });
    }

    /**
     * Validates if items in MultipartFile array are all images, else an error is thrown.
     * Note I am returning an array as it is a bit more efficient than arraylist in
     * terms of memory.
     *
     * @param multipartFiles is an array of {@link MultipartFile}.
     * @param defaultKey A property of {@link Product}.
     * @return A custom {@link CustomMultiPart} array.
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
