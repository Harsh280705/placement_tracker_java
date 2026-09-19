package com.example.placementtracker.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.example.placementtracker.model.Application;
import com.example.placementtracker.service.ApplicationService;
import com.example.placementtracker.service.ResourceNotFoundException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Controller tests: service is mocked, so no database is used.
 * Checks pages, redirects, validation errors and the 404 page.
 */
@WebMvcTest(ApplicationController.class)
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationService service;

    private Application sample() {
        Application app = new Application();
        app.setId(1L);
        app.setCompany("Acme Labs");
        app.setRole("Python Intern");
        app.setStatus("Applied");
        app.setAppliedOn(LocalDate.of(2026, 9, 16));
        return app;
    }

    @Test
    void dashboard_showsIndex() throws Exception {
        when(service.findAll()).thenReturn(List.of(sample()));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("applications"));
    }

    @Test
    void create_validInput_redirectsToDetails() throws Exception {
        Application saved = sample();
        when(service.create(any(Application.class))).thenReturn(saved);

        mockMvc.perform(post("/applications")
                        .param("company", "Acme Labs")
                        .param("role", "Python Intern")
                        .param("status", "Applied")
                        .param("appliedOn", "2026-09-16"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void create_blankCompany_showsFormWithErrors() throws Exception {
        mockMvc.perform(post("/applications")
                        .param("company", "")
                        .param("role", "Python Intern")
                        .param("status", "Applied")
                        .param("appliedOn", "2026-09-16"))
                .andExpect(status().isOk())
                .andExpect(view().name("application-form"))
                .andExpect(model().attributeHasFieldErrors("jobApplication", "company"));
    }

    @Test
    void create_appliedWithoutDate_showsFormWithErrors() throws Exception {
        mockMvc.perform(post("/applications")
                        .param("company", "Acme Labs")
                        .param("role", "Python Intern")
                        .param("status", "Applied"))
                .andExpect(status().isOk())
                .andExpect(view().name("application-form"))
                .andExpect(model().attributeHasFieldErrors("jobApplication", "appliedOn"));
    }

    @Test
    void detail_existingId_showsDetailPage() throws Exception {
        when(service.findByIdOrThrow(1L)).thenReturn(sample());

        mockMvc.perform(get("/applications/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("application-detail"));
    }

    @Test
    void detail_existingId_rendersDataAndRealIdLinks() throws Exception {
        when(service.findByIdOrThrow(1L)).thenReturn(sample());

        mockMvc.perform(get("/applications/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("application-detail"))
                .andExpect(model().attributeExists("jobApplication"))
                .andExpect(content().string(containsString("Acme Labs")))
                .andExpect(content().string(containsString("Python Intern")))
                .andExpect(content().string(containsString("/applications/1/edit")))
                .andExpect(content().string(containsString("/applications/1/delete")))
                .andExpect(content().string(not(containsString("/applications/null"))));
    }

    @Test
    void dashboard_rendersRowsWithRealIds() throws Exception {
        when(service.findAll()).thenReturn(List.of(sample()));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Acme Labs")))
                .andExpect(content().string(containsString("/applications/1\"")))
                .andExpect(content().string(containsString("/applications/1/edit")))
                .andExpect(content().string(not(containsString("/applications/null"))));
    }

    @Test
    void editPage_existingId_isPrefilled() throws Exception {
        when(service.findByIdOrThrow(1L)).thenReturn(sample());

        mockMvc.perform(get("/applications/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("application-form"))
                .andExpect(content().string(containsString("value=\"Acme Labs\"")))
                .andExpect(content().string(containsString("/applications/1/edit")));
    }

    @Test
    void detail_unknownId_shows404Page() throws Exception {
        when(service.findByIdOrThrow(999L))
                .thenThrow(new ResourceNotFoundException("Application not found: 999"));

        mockMvc.perform(get("/applications/999"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("404"));
    }

    @Test
    void update_validInput_redirectsToDetails() throws Exception {
        when(service.update(eq(1L), any(Application.class))).thenReturn(sample());

        mockMvc.perform(post("/applications/1/edit")
                        .param("company", "Acme Labs")
                        .param("role", "Graduate Engineer")
                        .param("status", "Interview")
                        .param("appliedOn", "2026-09-12"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void delete_existingId_redirectsToDashboard() throws Exception {
        mockMvc.perform(post("/applications/1/delete"))
                .andExpect(status().is3xxRedirection());
    }
}
