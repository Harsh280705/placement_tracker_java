package com.example.placementtracker.repository;

import com.example.placementtracker.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Database access for applications.
 * No SQL needed: save, findAll, findById, deleteById all come free
 * from JpaRepository (implemented by Spring Data JPA + Hibernate).
 */
@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
}
