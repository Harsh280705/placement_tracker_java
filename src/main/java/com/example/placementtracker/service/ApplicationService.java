package com.example.placementtracker.service;

import com.example.placementtracker.model.Application;
import com.example.placementtracker.repository.ApplicationRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * Business logic between the controller and the database.
 * Holds the rules that don't fit in simple annotations:
 * status whitelist + "appliedOn required unless Wishlist".
 */
@Service
public class ApplicationService {

    /** Allowed status values (must match the form dropdown). */
    public static final List<String> ALLOWED_STATUSES =
            List.of("Wishlist", "Applied", "Interview", "Offer", "Rejected");

    private final ApplicationRepository repository;

    /** Constructor injection (recommended over @Autowired fields). */
    public ApplicationService(ApplicationRepository repository) {
        this.repository = repository;
    }

    /** Dashboard: newest first. Demonstrates findAll. */
    public List<Application> findAll() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    /** Details/edit/delete: demonstrates findById. Throws 404 exception if missing. */
    public Application findByIdOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + id));
    }

    /** Create: validates then demonstrates save (insert). */
    public Application create(Application app) {
        validate(app);
        return repository.save(app);
    }

    /** Update: loads existing row, copies fields, validates, saves. Demonstrates save (update). */
    public Application update(Long id, Application updated) {
        Application existing = findByIdOrThrow(id);
        existing.setCompany(updated.getCompany());
        existing.setRole(updated.getRole());
        existing.setStatus(updated.getStatus());
        existing.setAppliedOn(updated.getAppliedOn());
        existing.setJobUrl(updated.getJobUrl());
        existing.setNotes(updated.getNotes());
        validate(existing);
        return repository.save(existing);
    }

    /** Delete: checks existence first (for 404), then demonstrates deleteById. */
    public void deleteById(Long id) {
        findByIdOrThrow(id);
        repository.deleteById(id);
    }

    /**
     * Backend validation for the two rules annotations can't express.
     * Called by create/update AND mirrored in the controller so the
     * error appears beside the right form field.
     */
    public void validate(Application app) {
        if (app.getStatus() == null || !ALLOWED_STATUSES.contains(app.getStatus())) {
            throw new IllegalArgumentException(
                    "Status must be one of: Wishlist, Applied, Interview, Offer, Rejected");
        }
        if (!"Wishlist".equals(app.getStatus()) && app.getAppliedOn() == null) {
            throw new IllegalArgumentException(
                    "Applied date is required unless status is Wishlist");
        }
    }
}
