package org.massage.parlor.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "specialists")
public class Specialist {
    
    @Id
    private Integer id;
    
    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;
    
    private Integer experience = 0;
    
    private Boolean active = true;
    
    @PrePersist
    protected void onCreate() {
        if (experience == null) {
            experience = 0;
        }
        if (active == null) {
            active = true;
        }
    }
}
