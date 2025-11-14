package com.example.demo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "student")
@Data
@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)  // PK만 사용
public class Student {
    @Id
    @EqualsAndHashCode.Include  // PK만 hashCode/equals에 포함
    private String studentId;

    private String name;
    private String password;

    @Column(name = "allowed", nullable = false, columnDefinition = "boolean default false")
    private boolean allowed = false;
}