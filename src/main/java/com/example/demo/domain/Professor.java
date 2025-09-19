package com.example.demo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "professor")
@Data
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Professor {
    @Id
    private String professorId;
    private String name;
    private String password;

    @Column(name = "allowed", nullable = false, columnDefinition = "boolean default false")
    private boolean allowed = false;  // 관리자 승인 여부
}