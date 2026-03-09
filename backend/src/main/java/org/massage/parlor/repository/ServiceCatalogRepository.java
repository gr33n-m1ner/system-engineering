package org.massage.parlor.repository;

import org.massage.parlor.model.ServiceCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceCatalogRepository extends JpaRepository<ServiceCatalog, Integer> {
    
    Optional<ServiceCatalog> findByIdAndActiveTrue(Integer id);
    
    List<ServiceCatalog> findAllByActiveTrue();
    
    Optional<ServiceCatalog> findByTitle(String title);
}
