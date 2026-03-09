package org.massage.parlor.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "services")
public class ServiceCatalog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(unique = true)
    private String title;
    
    private Boolean active = true;
    
    @PrePersist
    protected void onCreate() {
        if (active == null) {
            active = true;
        }
    }
}
