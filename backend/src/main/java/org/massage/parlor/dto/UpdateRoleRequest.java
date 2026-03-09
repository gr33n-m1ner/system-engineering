package org.massage.parlor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.massage.parlor.model.Role;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleRequest {
    private Role role;
}
