package com.cloudkitchen.dto.auth;

import com.cloudkitchen.entity.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponse {

    private String token;
    private String tokenType;
    private Long userId;
    private String fullName;
    private UserRole role;
}





