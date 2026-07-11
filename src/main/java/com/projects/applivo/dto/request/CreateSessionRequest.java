package com.projects.applivo.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateSessionRequest {

    @NotNull
    private Long appVersionId;

}
