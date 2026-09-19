package com.example.placementtracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.placementtracker.model.Application;
import com.example.placementtracker.repository.ApplicationRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

/**
 * Service unit tests: repository is mocked, so the real
 * placement_tracker.db is never touched.
 */
@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository repository;

    @InjectMocks
    private ApplicationService service;

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private Application validApp() {
        Application app = new Application();
        app.setCompany("Acme Labs");
        app.setRole("Python Intern");
        app.setStatus("Applied");
        app.setAppliedOn(LocalDate.of(2026, 9, 16));
        return app;
    }

    @Test
    void create_validApplication_savesIt() {
        Application app = validApp();
        when(repository.save(any(Application.class))).thenAnswer(i -> i.getArgument(0));

        Application saved = service.create(app);

        assertEquals("Acme Labs", saved.getCompany());
        verify(repository).save(app);
    }

    @Test
    void create_wishlistWithoutDate_isAllowed() {
        Application app = validApp();
        app.setStatus("Wishlist");
        app.setAppliedOn(null);
        when(repository.save(any(Application.class))).thenAnswer(i -> i.getArgument(0));

        service.create(app);

        verify(repository).save(app);
    }

    @Test
    void create_appliedWithoutDate_isRejected() {
        Application app = validApp();
        app.setAppliedOn(null);

        assertThrows(IllegalArgumentException.class, () -> service.create(app));
    }

    @Test
    void create_badStatus_isRejected() {
        Application app = validApp();
        app.setStatus("Hired");

        assertThrows(IllegalArgumentException.class, () -> service.create(app));
    }

    @Test
    void validation_companyTooShort_failsBeanValidation() {
        Application app = validApp();
        app.setCompany("A");

        assertTrue(validator.validate(app).stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("company")));
    }

    @Test
    void validation_notesOver1000Chars_failsBeanValidation() {
        Application app = validApp();
        app.setNotes("x".repeat(1001));

        assertTrue(validator.validate(app).stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("notes")));
    }

    @Test
    void findById_unknownId_throws404Exception() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findByIdOrThrow(999L));
    }

    @Test
    void update_existingRecord_copiesFieldsAndSaves() {
        Application existing = validApp();
        existing.setId(1L);
        Application changes = validApp();
        changes.setRole("Graduate Engineer");
        changes.setStatus("Interview");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Application.class))).thenAnswer(i -> i.getArgument(0));

        Application updated = service.update(1L, changes);

        assertEquals("Graduate Engineer", updated.getRole());
        assertEquals("Interview", updated.getStatus());
    }

    @Test
    void delete_existingRecord_callsDeleteById() {
        when(repository.findById(1L)).thenReturn(Optional.of(validApp()));

        service.deleteById(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void findAll_returnsRepositoryResults() {
        when(repository.findAll(any(Sort.class))).thenReturn(List.of(validApp()));

        assertEquals(1, service.findAll().size());
    }
}
