package com.hospital.platform.patients.domain;

import com.hospital.platform.patients.exception.InvalidDocumentException;
import java.util.Locale;

public final class DocumentIdentity {
    private DocumentIdentity() {
    }

    public static String type(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    public static String number(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    public static boolean isValid(String documentType, String documentNumber) {
        String type = type(documentType);
        String number = number(documentNumber);
        if (type == null || number == null) {
            return false;
        }
        return switch (type) {
            case "DNI" -> number.matches("[0-9]{8}");
            case "CE" -> number.matches("[A-Z0-9]{8,12}");
            case "PASSPORT" -> number.matches("[A-Z0-9]{6,12}");
            default -> false;
        };
    }

    public static void requireValid(String type, String number) {
        if (!isValid(type, number)) {
            throw new InvalidDocumentException();
        }
    }
}
