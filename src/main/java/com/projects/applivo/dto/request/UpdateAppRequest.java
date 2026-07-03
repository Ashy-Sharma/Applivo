package com.projects.applivo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAppRequest {

    @Size(max = 100)
    private String name;

    @Size(max = 200)
    private String description;

    @Size(max = 50)
    private String category;

    private String iconUrl;

}