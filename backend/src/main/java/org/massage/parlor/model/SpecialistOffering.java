package org.massage.parlor.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "specialist_services", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"specialist_id", "service_id", "price"})
})
public class SpecialistOffering {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialist_id")
    private Specialist specialist;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private ServiceCatalog serviceCatalog;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    
    private Boolean active = true;
    
    @PrePersist
    protected void onCreate() {
        if (active == null) {
            active = true;
        }
    }
}
