package org.massage.parlor.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(unique = true)
    private String login;
    
    private String passwordHash;
    
    private String name;
    
    @Enumerated(EnumType.STRING)
    private Role role = Role.CLIENT;
    
    @Column(unique = true)
    private String phone;
    
    private String additionalInfo;
    
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (role == null) {
            role = Role.CLIENT;
        }
    }
}
