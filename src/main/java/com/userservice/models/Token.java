package com.userservice.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class Token extends BaseModel{

    private String value;
    @ManyToOne
    private User user;
    private Long expiryAt;
}
