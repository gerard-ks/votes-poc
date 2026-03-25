package ci.org.votespoc.modules.votes.core.entities;

import ci.org.votespoc.shared.exceptions.ValidationException;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class Vote {
    private final UUID id;
    private final UUID userId;
    private final UUID pollId;
    private final UUID optionId;
    private final Instant votedAt;

    private Vote(UUID id, UUID userId, UUID pollId, UUID optionId, Instant votedAt) {
        this.id = id;
        this.userId = userId;
        this.pollId = pollId;
        this.optionId = optionId;
        this.votedAt = votedAt;
    }

    public static Vote create(UUID userId, UUID pollId, UUID optionId) {
        if (userId == null || pollId == null || optionId == null) {
            throw new ValidationException("userId, pollId et optionId sont obligatoires");
        }

        return new Vote(
                UUID.randomUUID(),
                userId,
                pollId,
                optionId,
                Instant.now()
        );
    }

    /**
     * Un participant ne peut voter qu'une seule fois par sondage
     */
    public boolean hasVotedInPoll(UUID otherPollId) {
        return this.pollId.equals(otherPollId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vote vote = (Vote) o;
        return id.equals(vote.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
