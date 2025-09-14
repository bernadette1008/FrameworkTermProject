package com.example.demo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "student")
@Data
@Setter
@Getter
public class Student {
    @Id
    private String studentId;  // 학번(PK)
    private String name;       // 이름
    private String password;   // 비밀번호
}