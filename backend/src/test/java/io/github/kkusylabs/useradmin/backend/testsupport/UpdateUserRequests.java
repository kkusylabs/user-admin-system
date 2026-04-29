package io.github.kkusylabs.useradmin.backend.testsupport;

import io.github.kkusylabs.useradmin.backend.dtos.user.UpdateUserRequest;
import io.github.kkusylabs.useradmin.backend.models.Role;
import org.openapitools.jackson.nullable.JsonNullable;

public final class UpdateUserRequests {

    private UpdateUserRequests() {}

    // ===== Builder entry point =====
    public static Builder updateRequest() {
        return new Builder();
    }

    public static UpdateUserRequest noChanges() {
        return updateRequest().build();
    }

    // ===== Convenience methods =====
    public static UpdateUserRequest withFullName(String fullName) {
        return updateRequest().fullName(fullName).build();
    }

    public static UpdateUserRequest withEmail(String email) {
        return updateRequest().email(email).build();
    }

    public static UpdateUserRequest withPhone(String phone) {
        return updateRequest().phone(phone).build();
    }

    public static UpdateUserRequest withJobTitle(String jobTitle) {
        return updateRequest().jobTitle(jobTitle).build();
    }

    public static UpdateUserRequest withActive(Boolean active) {
        return updateRequest().active(active).build();
    }

    public static UpdateUserRequest withDepartment(Long departmentId) {
        return updateRequest().departmentId(departmentId).build();
    }

    public static UpdateUserRequest withRole(Role role) {
        return updateRequest().role(role).build();
    }

    public static UpdateUserRequest withProfile(String fullName, String email, String phone) {
        return updateRequest()
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .build();
    }

    // ===== Builder =====
    public static class Builder {
        private JsonNullable<String> fullName = JsonNullable.undefined();
        private JsonNullable<String> email = JsonNullable.undefined();
        private JsonNullable<String> phone = JsonNullable.undefined();
        private JsonNullable<String> jobTitle = JsonNullable.undefined();
        private JsonNullable<Boolean> active = JsonNullable.undefined();
        private JsonNullable<Long> departmentId = JsonNullable.undefined();
        private JsonNullable<Role> role = JsonNullable.undefined();

        public Builder fullName(String value) {
            this.fullName = JsonNullable.of(value);
            return this;
        }

        public Builder email(String value) {
            this.email = JsonNullable.of(value);
            return this;
        }

        public Builder phone(String value) {
            this.phone = JsonNullable.of(value);
            return this;
        }

        public Builder jobTitle(String value) {
            this.jobTitle = JsonNullable.of(value);
            return this;
        }

        public Builder active(Boolean value) {
            this.active = JsonNullable.of(value);
            return this;
        }

        public Builder departmentId(Long value) {
            this.departmentId = JsonNullable.of(value);
            return this;
        }

        public Builder role(Role value) {
            this.role = JsonNullable.of(value);
            return this;
        }

        // Explicit nulls (for validator tests)
        public Builder nullFullName() {
            this.fullName = JsonNullable.of(null);
            return this;
        }

        public Builder nullEmail() {
            this.email = JsonNullable.of(null);
            return this;
        }

        public UpdateUserRequest build() {
            return new UpdateUserRequest(
                    fullName,
                    email,
                    phone,
                    jobTitle,
                    active,
                    departmentId,
                    role
            );
        }
    }
}
