package com.example.demo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "administrator")
@Data
@Setter
@Getter
public class Administrator {
    @Id
    private String id;
    private String username;
}
