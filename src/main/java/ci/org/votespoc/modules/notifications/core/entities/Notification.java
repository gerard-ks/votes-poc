package ci.org.votespoc.modules.notifications.core.entities;

import ci.org.votespoc.shared.exceptions.ValidationException;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
public class Notification {

    private final UUID id;
    private final UUID recipientId;
    private final String recipientAddress;
    private final NotificationChannel channel;
    private final NotificationType type;
    private NotificationStatus status;
    private final Map<String, Object> templateVariables;
    private final Instant createdAt;
    private Instant sentAt;
    private int retryCount;
    private String errorMessage;

    private Notification(UUID id, UUID recipientId, String recipientAddress,
                         NotificationChannel channel, NotificationType type,
                         Map<String, Object> templateVariables, Instant createdAt) {
        this.id = id;
        this.recipientId = recipientId;
        this.recipientAddress = recipientAddress;
        this.channel = channel;
        this.type = type;
        this.templateVariables = templateVariables;
        this.createdAt = createdAt;
        this.status = NotificationStatus.PENDING;
        this.retryCount = 0;
    }

    public static Notification create(UUID recipientId, String recipientAddress,
                                      NotificationChannel channel,
                                      NotificationType type,
                                      Map<String, Object> variables) {
        if (recipientId == null) {
            throw new ValidationException("recipientId est obligatoire");
        }
        if (recipientAddress == null || recipientAddress.isBlank()) {
            throw new ValidationException("recipientAddress est obligatoire");
        }
        if (channel == null) {
            throw new ValidationException("channel est obligatoire");
        }
        if (type == null) {
            throw new ValidationException("type est obligatoire");
        }
        if (variables == null) {
            throw new ValidationException("variables est obligatoire");
        }

        return new Notification(
                UUID.randomUUID(),
                recipientId,
                recipientAddress,
                channel,
                type,
                variables,
                Instant.now()
        );
    }

    /**
     * Incrémente le compteur de tentatives
     */
    public void markAsSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = Instant.now();
        this.errorMessage = null;
    }

    /**
     * Gère les retries (max 3)
     */
    public void markAsFailed(String error) {
        this.retryCount++;
        this.errorMessage = error;
        if (this.retryCount >= 3) {
            this.status = NotificationStatus.FAILED;
        } else {
            this.status = NotificationStatus.PENDING; // Retry
        }
    }

    /**
     * Vérifie si on peut retry
     */
    public boolean canRetry() {
        return this.retryCount < 3 && this.status == NotificationStatus.PENDING;
    }

    /**
     * Vérifie si la notification a été envoyée avec succès.
     */
    public boolean isSent() {
        return this.status == NotificationStatus.SENT;
    }

    /**
     * Vérifie si la notification a définitivement échoué.
     */
    public boolean isFailed() {
        return this.status == NotificationStatus.FAILED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
