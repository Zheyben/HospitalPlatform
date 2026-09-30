package com.hospital.platform.patients.exception;

import java.util.UUID;

public class UserAlreadyLinkedException extends RuntimeException {

    public UserAlreadyLinkedException(UUID userId) {
        super("User is already linked to an active patient: " + userId);
    }
}
