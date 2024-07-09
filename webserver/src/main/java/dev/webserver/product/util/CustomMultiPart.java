package dev.webserver.product.util;

import java.io.File;
import java.io.Serializable;
import java.util.Map;

// Needed to upload files to s3
public record CustomMultiPart(File file, Map<String, String> metadata, String key) implements Serializable { }