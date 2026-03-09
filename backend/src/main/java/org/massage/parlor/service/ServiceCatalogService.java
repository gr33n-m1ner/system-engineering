package org.massage.parlor.service;

import lombok.RequiredArgsConstructor;
import org.massage.parlor.dto.CreateServiceCatalogRequest;
import org.massage.parlor.exception.NotFoundException;
import org.massage.parlor.model.ServiceCatalog;
import org.massage.parlor.repository.ServiceCatalogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ServiceCatalogService {
    
    private final ServiceCatalogRepository serviceCatalogRepository;
    
    public List<ServiceCatalog> getAllServiceCatalogs() {
        return serviceCatalogRepository.findAllByActiveTrue();
    }
    
    public Optional<ServiceCatalog> getServiceCatalogById(Integer id) {
        return serviceCatalogRepository.findByIdAndActiveTrue(id);
    }
    
    @Transactional
    public ServiceCatalog createServiceCatalog(CreateServiceCatalogRequest request) {
        Optional<ServiceCatalog> existing = serviceCatalogRepository.findByTitle(request.getTitle());
        if (existing.isPresent()) {
            ServiceCatalog serviceCatalog = existing.get();
            if (!serviceCatalog.getActive()) {
                serviceCatalog.setActive(true);
                return serviceCatalogRepository.save(serviceCatalog);
            }
            return serviceCatalog;
        }
        
        ServiceCatalog serviceCatalog = new ServiceCatalog();
        serviceCatalog.setTitle(request.getTitle());
        return serviceCatalogRepository.save(serviceCatalog);
    }
    
    @Transactional
    public void deactivateServiceCatalog(Integer id) {
        ServiceCatalog serviceCatalog = serviceCatalogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Service catalog with id " + id + " not found"));
        serviceCatalog.setActive(false);
        serviceCatalogRepository.save(serviceCatalog);
    }
}
