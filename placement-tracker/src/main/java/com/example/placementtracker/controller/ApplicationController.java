package com.example.placementtracker.controller;

import com.example.placementtracker.model.Application;
import com.example.placementtracker.service.ApplicationService;
import com.example.placementtracker.service.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Browser pages (Spring MVC, not a REST API).
 * Each method: take HTTP in -> call Service -> pick a Thymeleaf page.
 */
@Controller
public class ApplicationController {

    private final ApplicationService service;

    public ApplicationController(ApplicationService service) {
        this.service = service;
    }

    /** 1. Dashboard: GET / — table of all applications (or empty state). */
    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("applications", service.findAll());
        return "index";
    }

    /** 2. Blank create form: GET /applications/new */
    @GetMapping("/applications/new")
    public String showCreateForm(Model model) {
        Application app = new Application();
        app.setStatus("Applied"); // sensible default in the dropdown
        model.addAttribute("jobApplication", app);
        model.addAttribute("statuses", ApplicationService.ALLOWED_STATUSES);
        model.addAttribute("formTitle", "Add Application");
        model.addAttribute("formAction", "/applications");
        return "application-form";
    }

    /** 3. Create submit: POST /applications — validate, save, redirect to details. */
    @PostMapping("/applications")
    public String create(@Valid @ModelAttribute("jobApplication") Application app,
                         BindingResult bindingResult,
                         Model model) {
        applyCustomRules(app, bindingResult);
        if (bindingResult.hasErrors()) {
            // Re-show form WITH entered values (th:object keeps them) + error messages.
            model.addAttribute("statuses", ApplicationService.ALLOWED_STATUSES);
            model.addAttribute("formTitle", "Add Application");
            model.addAttribute("formAction", "/applications");
            return "application-form";
        }
        try {
            Application saved = service.create(app);
            return "redirect:/applications/" + saved.getId();
        } catch (IllegalArgumentException ex) {
            mapServiceError(ex, bindingResult);
            model.addAttribute("statuses", ApplicationService.ALLOWED_STATUSES);
            model.addAttribute("formTitle", "Add Application");
            model.addAttribute("formAction", "/applications");
            return "application-form";
        }
    }

    /** 4. Details: GET /applications/{id} — 404 if id missing. */
    @GetMapping("/applications/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("jobApplication", service.findByIdOrThrow(id));
        return "application-detail";
    }

    /** 5. Pre-filled edit form: GET /applications/{id}/edit */
    @GetMapping("/applications/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("jobApplication", service.findByIdOrThrow(id));
        model.addAttribute("statuses", ApplicationService.ALLOWED_STATUSES);
        model.addAttribute("formTitle", "Edit Application");
        model.addAttribute("formAction", "/applications/" + id + "/edit");
        return "application-form";
    }

    /** 6. Edit submit: POST /applications/{id}/edit — validate, update, redirect to details. */
    @PostMapping("/applications/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("jobApplication") Application app,
                         BindingResult bindingResult,
                         Model model) {
        applyCustomRules(app, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("statuses", ApplicationService.ALLOWED_STATUSES);
            model.addAttribute("formTitle", "Edit Application");
            model.addAttribute("formAction", "/applications/" + id + "/edit");
            return "application-form";
        }
        try {
            service.update(id, app);
            return "redirect:/applications/" + id;
        } catch (IllegalArgumentException ex) {
            mapServiceError(ex, bindingResult);
            model.addAttribute("statuses", ApplicationService.ALLOWED_STATUSES);
            model.addAttribute("formTitle", "Edit Application");
            model.addAttribute("formAction", "/applications/" + id + "/edit");
            return "application-form";
        }
    }

    /** 7. Delete: POST only (never a GET link) + browser confirm() in the template. */
    @PostMapping("/applications/{id}/delete")
    public String delete(@PathVariable Long id) {
        service.deleteById(id);
        return "redirect:/";
    }

    // --- Helpers ---

    /**
     * The special rule "@NotBlank/@Size can't express":
     * appliedOn is required unless status is Wishlist + status whitelist.
     * Adds errors beside the right field (shown via th:errors).
     */
    private void applyCustomRules(Application app, BindingResult bindingResult) {
        if (app.getStatus() != null && !ApplicationService.ALLOWED_STATUSES.contains(app.getStatus())) {
            bindingResult.rejectValue("status", "status.invalid",
                    "Status must be one of: Wishlist, Applied, Interview, Offer, Rejected");
        }
        if (app.getStatus() != null
                && !"Wishlist".equals(app.getStatus())
                && app.getAppliedOn() == null) {
            bindingResult.rejectValue("appliedOn", "appliedOn.required",
                    "Applied date is required unless status is Wishlist");
        }
    }

    /** Converts a service IllegalArgumentException into the matching field error. */
    private void mapServiceError(IllegalArgumentException ex, BindingResult bindingResult) {
        String msg = ex.getMessage();
        if (msg != null && msg.startsWith("Status")) {
            bindingResult.rejectValue("status", "status.invalid", msg);
        } else {
            bindingResult.rejectValue("appliedOn", "appliedOn.required", msg);
        }
    }

    /** Unknown id -> custom 404.html page with HTTP 404 (never crashes). */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        return "404";
    }

    /** Non-numeric id (e.g. /applications/abc or /applications/null) -> custom 404 page, not Whitelabel 400. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleBadId(MethodArgumentTypeMismatchException ex, Model model) {
        model.addAttribute("message", "Application not found: " + ex.getValue());
        return "404";
    }
}
