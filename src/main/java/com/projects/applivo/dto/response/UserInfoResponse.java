package com.projects.applivo.dto.response;

import com.projects.applivo.entity.Role;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserInfoResponse {

    private Long id;

    private String email;

    private String username;

    private Role role;

    private String avatarUrl;

}
