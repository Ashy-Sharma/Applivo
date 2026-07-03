package com.projects.applivo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class UploadVersionRequest {

    @NotBlank
    @Size(max = 50)
    private String versionTag;

}
