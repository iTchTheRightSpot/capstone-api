package com.sarabrandserver.util;

import com.sarabrandserver.product.util.CreateProductDTO;
import org.springframework.mock.web.MockMultipartFile;

public record Result(CreateProductDTO dto, MockMultipartFile[] files) { }
