package org.massage.parlor.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.massage.parlor.model.Role;

@Data
public class AuthResponse {
    private final String token;
    private final String login;
    private final Role role;
}
