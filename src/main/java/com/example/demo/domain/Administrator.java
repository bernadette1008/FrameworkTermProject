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
    private String administratorId;  // 관리자 ID (PK)
    private String name;             // 관리자 이름
    private String password;         // 관리자 비밀번호
}