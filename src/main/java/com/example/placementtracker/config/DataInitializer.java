package com.example.placementtracker.config;

import com.example.placementtracker.model.Application;
import com.example.placementtracker.repository.ApplicationRepository;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Inserts 3 sample rows on first run (only when the table is empty).
 * Makes the dashboard non-empty for demos; safe to delete afterwards.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final ApplicationRepository repository;

    public DataInitializer(ApplicationRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        repository.save(sample("Acme Labs", "Python Intern", "Applied",
                LocalDate.of(2026, 9, 16), "", "First application"));
        repository.save(sample("Northstar", "Graduate Engineer", "Interview",
                LocalDate.of(2026, 9, 12), "", "Technical round done"));
        repository.save(sample("Contoso", "Backend Intern", "Rejected",
                LocalDate.of(2026, 9, 5), "", "Keep referral for later"));
    }

    private Application sample(String company, String role, String status,
                               LocalDate appliedOn, String jobUrl, String notes) {
        Application app = new Application();
        app.setCompany(company);
        app.setRole(role);
        app.setStatus(status);
        app.setAppliedOn(appliedOn);
        app.setJobUrl(jobUrl);
        app.setNotes(notes);
        return app;
    }
}
