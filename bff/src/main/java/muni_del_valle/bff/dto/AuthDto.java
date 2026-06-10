package muni_del_valle.bff.dto;

import lombok.Data;

@Data
public class AuthDto {
    private String email;
    private String password;
    private String fullName;
    private String token;
}