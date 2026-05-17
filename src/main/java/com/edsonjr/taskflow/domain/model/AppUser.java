package com.edsonjr.taskflow.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "app_users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_app_users_email", columnNames = "email")
        }
)
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @NotBlank(message = "name is required")
    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @NotBlank(message = "email must not be blank")
    @Email(message = "email must be valid")
    @Column(name = "email", nullable = false, length = 254)
    private String email;

    protected AppUser() {
    }

    private AppUser(String name, String email) {
        this.name = requireNonBlank(name, "name");
        this.email = requireNonBlank(email, "email").toLowerCase(Locale.ROOT);
    }

    public static AppUser create(String name, String email) {
        return new AppUser(name, email);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    private static String requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " is required");

        String normalizedValue = value.trim();

        if (normalizedValue.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }

        return normalizedValue;
    }
}
