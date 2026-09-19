package com.example.placementtracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * One row in the "applications" table.
 * Each field maps to a column via Hibernate.
 */
@Entity
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Company is required")
    @Size(min = 2, max = 100, message = "Company must be 2-100 characters")
    @Column(nullable = false, length = 100)
    private String company;

    @NotBlank(message = "Role is required")
    @Size(min = 2, max = 100, message = "Role must be 2-100 characters")
    @Column(nullable = false, length = 100)
    private String role;

    // Kept as String (not Enum) so the HTML form binds easily
    // and wrong values can show a friendly error instead of crashing.
    @NotBlank(message = "Status is required")
    @Column(nullable = false, length = 20)
    private String status;

    // Required unless status is Wishlist (checked in ApplicationService).
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "applied_on")
    private LocalDate appliedOn;

    @Size(max = 500, message = "Job URL must be under 500 characters")
    @Column(name = "job_url", length = 500)
    private String jobUrl;

    @Size(max = 1000, message = "Notes must be under 1000 characters")
    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** JPA needs a no-args constructor. */
    public Application() {
    }

    /** Auto-fill creation time on first save. */
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // --- Getters and setters (needed by JPA, Thymeleaf and forms) ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getAppliedOn() {
        return appliedOn;
    }

    public void setAppliedOn(LocalDate appliedOn) {
        this.appliedOn = appliedOn;
    }

    public String getJobUrl() {
        return jobUrl;
    }

    public void setJobUrl(String jobUrl) {
        this.jobUrl = jobUrl;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
