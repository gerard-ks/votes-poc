package ci.org.votespoc.modules.users.core.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    PARTICIPANT("participant"),  // Peut voter, voir les résultats
    CREATOR("creator"),          // Peut créer, modifier, clôturer ses sondages
    ADMIN("admin");              // Peut tout faire

    private final String value;
}
