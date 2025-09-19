package com.example.demo.repository;

import com.example.demo.domain.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdministratorRepository extends JpaRepository<Administrator, String> {

    Administrator findByAdministratorId(String administratorId);
    boolean existsByAdministratorId(String administratorId);
    Administrator findByAdministratorIdAndPassword(String administratorId, String password);
}