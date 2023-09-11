package com.emmanuel.sarabrandserver.util;

import com.emmanuel.sarabrandserver.product.util.CreateProductDTO;
import org.springframework.mock.web.MockMultipartFile;

public record Result(CreateProductDTO dto, MockMultipartFile[] files) { }
