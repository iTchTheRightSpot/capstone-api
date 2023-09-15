package com.emmanuel.sarabrandserver.category.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UpdateCollectionDTO {

    @NotNull(message = "cannot be empty")
    @NotEmpty(message = "cannot be empty")
    private String id;

    @NotNull(message = "cannot be empty")
    @NotEmpty(message = "cannot be empty")
    @Size(max = 50, message = "category name cannot exceed length of 50")
    private String name;

    @NotNull(message = "cannot be empty")
    private Boolean visible;

}
