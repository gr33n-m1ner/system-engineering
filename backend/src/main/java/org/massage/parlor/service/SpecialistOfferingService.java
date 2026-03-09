package org.massage.parlor.service;

import lombok.RequiredArgsConstructor;
import org.massage.parlor.dto.AddSpecialistOfferingRequest;
import org.massage.parlor.exception.NotFoundException;
import org.massage.parlor.model.ServiceCatalog;
import org.massage.parlor.model.Specialist;
import org.massage.parlor.model.SpecialistOffering;
import org.massage.parlor.repository.ServiceCatalogRepository;
import org.massage.parlor.repository.SpecialistRepository;
import org.massage.parlor.repository.SpecialistOfferingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SpecialistOfferingService {
    
    private final SpecialistOfferingRepository specialistOfferingRepository;
    private final SpecialistRepository specialistRepository;
    private final ServiceCatalogRepository serviceCatalogRepository;
    
    public List<SpecialistOffering> getOfferingsBySpecialistId(Integer specialistId) {
        return specialistOfferingRepository.findBySpecialistIdAndActiveTrue(specialistId);
    }
    
    public Optional<SpecialistOffering> getSpecialistOfferingById(Integer id) {
        return specialistOfferingRepository.findByIdAndActiveTrue(id);
    }
    
    @Transactional
    public SpecialistOffering addOfferingToSpecialist(Integer specialistId, AddSpecialistOfferingRequest request) {
        Optional<SpecialistOffering> existing = specialistOfferingRepository
                .findBySpecialistIdAndServiceCatalogIdAndPrice(
                        specialistId, request.getServiceCatalogId(), request.getPrice());
        
        if (existing.isPresent()) {
            SpecialistOffering specialistOffering = existing.get();
            if (!specialistOffering.getActive()) {
                specialistOffering.setActive(true);
                return specialistOfferingRepository.save(specialistOffering);
            }
            return specialistOffering;
        }
        
        Specialist specialist = specialistRepository.findByIdAndActiveTrue(specialistId)
                .orElseThrow(() -> new NotFoundException("Specialist with id " + specialistId + " not found"));
        
        ServiceCatalog serviceCatalog = serviceCatalogRepository.findByIdAndActiveTrue(request.getServiceCatalogId())
                .orElseThrow(() -> new NotFoundException("Service catalog with id " + request.getServiceCatalogId() 
                        + " not found"));
        
        SpecialistOffering specialistOffering = new SpecialistOffering();
        specialistOffering.setSpecialist(specialist);
        specialistOffering.setServiceCatalog(serviceCatalog);
        specialistOffering.setPrice(request.getPrice());
        
        return specialistOfferingRepository.save(specialistOffering);
    }
    
    @Transactional
    public void deactivateSpecialistOffering(Integer id) {
        SpecialistOffering specialistOffering = specialistOfferingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Specialist offering with id " + id + " not found"));
        specialistOffering.setActive(false);
        specialistOfferingRepository.save(specialistOffering);
    }
}
