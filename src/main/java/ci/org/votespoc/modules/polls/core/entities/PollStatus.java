package ci.org.votespoc.modules.polls.core.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PollStatus {
    ACTIVE("active"),     // Accepte des votes
    CLOSED("closed"),     // N'accepte plus de votes
    ARCHIVED("archived"); // Conservé pour historique

    private final String value;
}
