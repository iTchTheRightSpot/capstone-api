package dev.webserver.external.aws;

import jakarta.validation.constraints.NotNull;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface IS3Service {
    void uploadToS3(@NotNull final File file, @NotNull final Map<String, String> metadata, @NotNull final String bucket, @NotNull final String key);
    void deleteFromS3(@NotNull final List<ObjectIdentifier> keys, @NotNull final String bucket);
    String preSignedUrl(@NotNull final String bucket, @NotNull final String key);
}
