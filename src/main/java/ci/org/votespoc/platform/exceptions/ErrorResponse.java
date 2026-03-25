package ci.org.votespoc.platform.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class ErrorResponse {

    private final String code;
    private final String message;
    private final Instant timestamp;
}
