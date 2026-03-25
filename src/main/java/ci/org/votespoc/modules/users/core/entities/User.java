package ci.org.votespoc.modules.users.core.entities;

import ci.org.votespoc.shared.exceptions.ValidationException;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class User {
    private final UUID id;
    private final String email;
    private final String name;
    private final UserRole role;
    private boolean emailVerified;
    private final Instant createdAt;

    private User(UUID id, String email, String name, UserRole role, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
        this.createdAt = createdAt;
    }

    public static User create(String email, String name, UserRole role) {

        if (email == null || email.isBlank()) {
            throw new ValidationException("Email is required");
        }

        if (name == null || name.isBlank()) {
            throw new ValidationException("Name is required");
        }

        return new User(
                UUID.randomUUID(),
                email.toLowerCase().trim(),
                name.trim(),
                role,
                Instant.now());
    }

    /**
     * Vérifie l'email pour recevoir des notifications
     */
    public void verifyEmail(String token) {
        if (token == null || token.isBlank()) {
            throw new ValidationException("Verification token is required");
        }
        this.emailVerified = true;
    }


    /**
     * Un utilisateur ne reçoit des notifications que si email vérifié
     */
    public boolean canReceiveNotifications() {
        return this.emailVerified;
    }

    /**
     * Un utilisateur doit être authentifié pour voter
     */
    public boolean canVote() {
        return this.role == UserRole.CREATOR || this.role == UserRole.PARTICIPANT;
    }

    /**
     * Un créateur ne peut pas dépasser 10 sondages actifs
     */
    public boolean canCreatePoll(int currentActivePollsCount) {
        if (this.role != UserRole.CREATOR && this.role != UserRole.ADMIN) {
            return false;
        }
        return currentActivePollsCount < 10;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
