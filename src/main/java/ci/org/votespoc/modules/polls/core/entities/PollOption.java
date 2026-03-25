package ci.org.votespoc.modules.polls.core.entities;

import ci.org.votespoc.shared.exceptions.ValidationException;
import lombok.Getter;

import java.util.UUID;

@Getter
public class PollOption {

    private final UUID id;
    private final String text;
    private int voteCount;

    private PollOption(UUID id, String text) {
        this.id = id;
        this.text = text;
        this.voteCount = 0;
    }

    public static PollOption create(String text) {
        if (text == null || text.isBlank()) {
            throw new ValidationException("Le texte de l'option est obligatoire");
        }
        return new PollOption(UUID.randomUUID(), text.trim());
    }

    /**
     * Incrémente le compteur de votes (appelé par le module votes/)
     */
    public void incrementVoteCount() {
        this.voteCount++;
    }

    /**
     * Pour calculer les pourcentages
     */
    public double getPercentage(int totalVotes) {
        if (totalVotes == 0) return 0.0;
        return Math.round((voteCount * 100.0 / totalVotes) * 10.0) / 10.0;
    }
}
