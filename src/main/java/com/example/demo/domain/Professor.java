package com.example.demo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Professor {
    @Id
    private String p_id;
    private String p_name;
    private String p_password;
}
