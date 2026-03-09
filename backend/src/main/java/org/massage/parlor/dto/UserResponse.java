package org.massage.parlor.dto;

import lombok.Data;
import org.massage.parlor.model.Role;

import java.time.LocalDateTime;

@Data
public class UserResponse {
    private final Integer id;
    private final String login;
    private final String name;
    private final Role role;
    private final String phone;
    private final String additionalInfo;
    private final LocalDateTime createdAt;
}
