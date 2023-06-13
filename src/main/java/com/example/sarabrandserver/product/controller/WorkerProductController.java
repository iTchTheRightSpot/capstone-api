package com.example.sarabrandserver.product.controller;

import com.example.sarabrandserver.product.dto.CreateProductDTO;
import com.example.sarabrandserver.product.service.WorkerProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/v1/worker/product")
public class WorkerProductController {

    private final WorkerProductService workerProductService;

    public WorkerProductController(WorkerProductService workerProductService) {
        this.workerProductService = workerProductService;
    }

    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize(value = "hasAuthority('WORKER')")
    public ResponseEntity<?> create(
            @Valid @ModelAttribute CreateProductDTO dto,
            @RequestParam(name = "file") MultipartFile file
    ) {
        return workerProductService.create(dto, file);
    }

}
