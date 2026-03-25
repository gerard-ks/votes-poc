package ci.org.votespoc.modules.polls.core.entities;

import ci.org.votespoc.shared.exceptions.ConflictException;
import ci.org.votespoc.shared.exceptions.UnauthorizedException;
import ci.org.votespoc.shared.exceptions.ValidationException;
import lombok.Getter;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
public class Poll {
    private final UUID id;
    private final UUID creatorId;
    private String title;
    private final List<PollOption> options;
    private Instant endsAt;
    private PollStatus status;
    private final Instant createdAt;

    private Poll(UUID id, UUID creatorId, String title, List<PollOption> options,
                 Instant endsAt, Instant createdAt) {
        this.id = id;
        this.creatorId = creatorId;
        this.title = title;
        this.options = options;
        this.endsAt = endsAt;
        this.status = PollStatus.ACTIVE;
        this.createdAt = createdAt;
    }

    public static Poll create(UUID creatorId, String title, List<PollOption> options, Instant endsAt) {
        // Titre non vide
        if (title == null || title.isBlank()) {
            throw new ValidationException("Le titre est obligatoire");
        }

        // Au moins 2 options
        if (options == null || options.size() < 2) {
            throw new ValidationException("Un sondage doit avoir au moins 2 options");
        }

        //Date de fin dans le futur
        if (endsAt == null || endsAt.isBefore(Instant.now())) {
            throw new ValidationException("La date de fin doit être dans le futur");
        }

        // Texte des options non vide
        for (PollOption option : options) {
            if (option.getText() == null || option.getText().isBlank()) {
                throw new ValidationException("Le texte d'une option ne peut pas être vide");
            }
        }

        return new Poll(
                UUID.randomUUID(),
                creatorId,
                title.trim(),
                new ArrayList<>(options),
                endsAt,
                Instant.now()
        );
    }

    /**
     * Un sondage n'accepte des votes que s'il est ACTIF
     */
    public boolean isActive() {
        return this.status == PollStatus.ACTIVE
                && this.endsAt.isAfter(Instant.now());
    }

    /**
     * Vérifie si on peut voter
     */
    public void ensureVotable() {
        if (!isActive()) {
            throw new ValidationException("Le sondage n'est plus actif");
        }
    }

    /**
     *  Seul le créateur peut clôturer
     */
    public void close(UUID currentUserId) {
        if (!this.creatorId.equals(currentUserId)) {
            throw new UnauthorizedException("Seul le créateur peut clôturer ce sondage");
        }
        if (this.status == PollStatus.CLOSED) {
            throw new ConflictException("Le sondage est déjà clos");
        }
        this.status = PollStatus.CLOSED;
    }

    /**
     *  Clôture manuelle avant date de fin
     */
    public void closeManually(UUID currentUserId) {
        close(currentUserId); // Même logique
    }

    /**
     * Pour calculer les résultats
     */
    public int getTotalVotes() {
        return options.stream()
                .mapToInt(PollOption::getVoteCount)
                .sum();
    }

    public List<PollOption> getOptions() {
        return Collections.unmodifiableList(options);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Poll poll = (Poll) o;
        return id.equals(poll.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
