package com.example.placementtracker.service;

/**
 * Thrown when an application id does not exist.
 * The controller catches this and shows the custom 404 page.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
