package dev.capstone.product.response;

import java.io.File;
import java.util.Map;

// Needed to upload files to s3
public record CustomMultiPart(File file, Map<String, String> metadata, String key) { }