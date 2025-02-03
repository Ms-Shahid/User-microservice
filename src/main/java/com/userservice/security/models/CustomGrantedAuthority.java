package com.userservice.security.models;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.userservice.models.Role;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

@JsonSerialize
@Getter
@Setter
public class CustomGrantedAuthority implements GrantedAuthority {
    private String authority;

    public CustomGrantedAuthority(Role role){
        this.authority = role.getValue();

    }

    @Override
    public String getAuthority(){
        return authority;
    }
}
