package org.massage.parlor.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.massage.parlor.dto.AddSpecialistOfferingRequest;
import org.massage.parlor.exception.NotFoundException;
import org.massage.parlor.model.ServiceCatalog;
import org.massage.parlor.model.Specialist;
import org.massage.parlor.model.SpecialistOffering;
import org.massage.parlor.repository.ServiceCatalogRepository;
import org.massage.parlor.repository.SpecialistRepository;
import org.massage.parlor.repository.SpecialistOfferingRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpecialistOfferingServiceTest {
    
    private static final Integer SPECIALIST_ID = 1;
    private static final Integer SERVICE_CATALOG_ID = 1;
    private static final BigDecimal PRICE = new BigDecimal("50.00");
    
    @Mock
    private SpecialistOfferingRepository specialistOfferingRepository;
    
    @Mock
    private SpecialistRepository specialistRepository;
    
    @Mock
    private ServiceCatalogRepository serviceCatalogRepository;
    
    @InjectMocks
    private SpecialistOfferingService specialistOfferingService;
    
    @Test
    void shouldAddOfferingToSpecialist() {
        Specialist specialist = new Specialist();
        specialist.setId(SPECIALIST_ID);
        specialist.setActive(true);
        
        ServiceCatalog serviceCatalog = new ServiceCatalog();
        serviceCatalog.setId(SERVICE_CATALOG_ID);
        serviceCatalog.setActive(true);
        
        AddSpecialistOfferingRequest request = new AddSpecialistOfferingRequest(SERVICE_CATALOG_ID, PRICE);
        
        when(specialistOfferingRepository.findBySpecialistIdAndServiceCatalogIdAndPrice(
                SPECIALIST_ID, SERVICE_CATALOG_ID, PRICE)).thenReturn(Optional.empty());
        when(specialistRepository.findByIdAndActiveTrue(SPECIALIST_ID)).thenReturn(Optional.of(specialist));
        when(serviceCatalogRepository.findByIdAndActiveTrue(SERVICE_CATALOG_ID)).thenReturn(Optional.of(serviceCatalog));
        when(specialistOfferingRepository.save(any(SpecialistOffering.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        SpecialistOffering result = specialistOfferingService.addOfferingToSpecialist(SPECIALIST_ID, request);
        
        assertNotNull(result);
        assertEquals(PRICE, result.getPrice());
        verify(specialistOfferingRepository).save(any(SpecialistOffering.class));
    }
    
    @Test
    void shouldReactivateExistingInactiveOffering() {
        AddSpecialistOfferingRequest request = new AddSpecialistOfferingRequest(SERVICE_CATALOG_ID, PRICE);
        SpecialistOffering existingOffering = new SpecialistOffering();
        existingOffering.setId(1);
        existingOffering.setPrice(PRICE);
        existingOffering.setActive(false);
        
        when(specialistOfferingRepository.findBySpecialistIdAndServiceCatalogIdAndPrice(
                SPECIALIST_ID, SERVICE_CATALOG_ID, PRICE)).thenReturn(Optional.of(existingOffering));
        when(specialistOfferingRepository.save(any(SpecialistOffering.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        SpecialistOffering result = specialistOfferingService.addOfferingToSpecialist(SPECIALIST_ID, request);
        
        assertNotNull(result);
        assertTrue(result.getActive());
        verify(specialistOfferingRepository).save(existingOffering);
    }
    
    @Test
    void shouldReturnExistingActiveOffering() {
        AddSpecialistOfferingRequest request = new AddSpecialistOfferingRequest(SERVICE_CATALOG_ID, PRICE);
        SpecialistOffering existingOffering = new SpecialistOffering();
        existingOffering.setId(1);
        existingOffering.setPrice(PRICE);
        existingOffering.setActive(true);
        
        when(specialistOfferingRepository.findBySpecialistIdAndServiceCatalogIdAndPrice(
                SPECIALIST_ID, SERVICE_CATALOG_ID, PRICE)).thenReturn(Optional.of(existingOffering));
        
        SpecialistOffering result = specialistOfferingService.addOfferingToSpecialist(SPECIALIST_ID, request);
        
        assertNotNull(result);
        assertTrue(result.getActive());
        verify(specialistOfferingRepository, never()).save(any());
    }
    
    @Test
    void shouldThrowExceptionWhenAddingOfferingToNonexistentSpecialist() {
        AddSpecialistOfferingRequest request = new AddSpecialistOfferingRequest(SERVICE_CATALOG_ID, PRICE);
        
        when(specialistOfferingRepository.findBySpecialistIdAndServiceCatalogIdAndPrice(
                SPECIALIST_ID, SERVICE_CATALOG_ID, PRICE)).thenReturn(Optional.empty());
        when(specialistRepository.findByIdAndActiveTrue(SPECIALIST_ID)).thenReturn(Optional.empty());
        
        assertThrows(NotFoundException.class, 
                () -> specialistOfferingService.addOfferingToSpecialist(SPECIALIST_ID, request));
    }
    
    @Test
    void shouldThrowExceptionWhenAddingNonexistentServiceCatalog() {
        Specialist specialist = new Specialist();
        specialist.setActive(true);
        AddSpecialistOfferingRequest request = new AddSpecialistOfferingRequest(SERVICE_CATALOG_ID, PRICE);
        
        when(specialistOfferingRepository.findBySpecialistIdAndServiceCatalogIdAndPrice(
                SPECIALIST_ID, SERVICE_CATALOG_ID, PRICE)).thenReturn(Optional.empty());
        when(specialistRepository.findByIdAndActiveTrue(SPECIALIST_ID)).thenReturn(Optional.of(specialist));
        when(serviceCatalogRepository.findByIdAndActiveTrue(SERVICE_CATALOG_ID)).thenReturn(Optional.empty());
        
        assertThrows(NotFoundException.class, 
                () -> specialistOfferingService.addOfferingToSpecialist(SPECIALIST_ID, request));
    }
    
    @Test
    void shouldDeactivateSpecialistOffering() {
        SpecialistOffering specialistOffering = new SpecialistOffering();
        specialistOffering.setId(1);
        specialistOffering.setActive(true);
        
        when(specialistOfferingRepository.findById(1)).thenReturn(Optional.of(specialistOffering));
        when(specialistOfferingRepository.save(any(SpecialistOffering.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        specialistOfferingService.deactivateSpecialistOffering(1);
        
        assertFalse(specialistOffering.getActive());
        verify(specialistOfferingRepository).save(specialistOffering);
    }
    
    @Test
    void shouldThrowExceptionWhenDeactivatingNonexistentSpecialistOffering() {
        when(specialistOfferingRepository.findById(999)).thenReturn(Optional.empty());
        
        assertThrows(NotFoundException.class, 
                () -> specialistOfferingService.deactivateSpecialistOffering(999));
    }
}
