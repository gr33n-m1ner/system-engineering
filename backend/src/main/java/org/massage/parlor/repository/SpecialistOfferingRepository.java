package org.massage.parlor.repository;

import org.massage.parlor.model.SpecialistOffering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpecialistOfferingRepository extends JpaRepository<SpecialistOffering, Integer> {
    
    List<SpecialistOffering> findBySpecialistIdAndActiveTrue(Integer specialistId);
    
    Optional<SpecialistOffering> findByIdAndActiveTrue(Integer id);
    
    Optional<SpecialistOffering> findBySpecialistIdAndServiceCatalogIdAndPrice(
            Integer specialistId, Integer serviceCatalogId, BigDecimal price);
}
