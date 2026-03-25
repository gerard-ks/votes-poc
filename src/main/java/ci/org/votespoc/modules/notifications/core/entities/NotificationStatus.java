package ci.org.votespoc.modules.notifications.core.entities;

public enum NotificationStatus {
    PENDING,  // En attente d'envoi
    SENT,     // Envoyé avec succès
    FAILED    // Échec définitif (après 3 tentatives)
}
