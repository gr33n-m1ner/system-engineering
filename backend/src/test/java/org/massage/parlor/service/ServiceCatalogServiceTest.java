package org.massage.parlor.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.massage.parlor.dto.CreateServiceCatalogRequest;
import org.massage.parlor.exception.NotFoundException;
import org.massage.parlor.model.ServiceCatalog;
import org.massage.parlor.repository.ServiceCatalogRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceCatalogServiceTest {
    
    private static final Integer ID = 1;
    private static final String TITLE = "Swedish Massage";
    
    @Mock
    private ServiceCatalogRepository serviceCatalogRepository;
    
    @InjectMocks
    private ServiceCatalogService serviceCatalogService;
    
    @Test
    void shouldCreateServiceCatalog() {
        CreateServiceCatalogRequest request = new CreateServiceCatalogRequest(TITLE);
        ServiceCatalog savedServiceCatalog = new ServiceCatalog(ID, TITLE, true);
        
        when(serviceCatalogRepository.findByTitle(TITLE)).thenReturn(Optional.empty());
        when(serviceCatalogRepository.save(any(ServiceCatalog.class))).thenReturn(savedServiceCatalog);
        
        ServiceCatalog result = serviceCatalogService.createServiceCatalog(request);
        
        assertNotNull(result);
        assertEquals(TITLE, result.getTitle());
        verify(serviceCatalogRepository).save(any(ServiceCatalog.class));
    }
    
    @Test
    void shouldReactivateExistingInactiveServiceCatalog() {
        CreateServiceCatalogRequest request = new CreateServiceCatalogRequest(TITLE);
        ServiceCatalog existingServiceCatalog = new ServiceCatalog(ID, TITLE, false);
        
        when(serviceCatalogRepository.findByTitle(TITLE)).thenReturn(Optional.of(existingServiceCatalog));
        when(serviceCatalogRepository.save(any(ServiceCatalog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        ServiceCatalog result = serviceCatalogService.createServiceCatalog(request);
        
        assertNotNull(result);
        assertEquals(TITLE, result.getTitle());
        assertTrue(result.getActive());
        verify(serviceCatalogRepository).save(existingServiceCatalog);
    }
    
    @Test
    void shouldReturnExistingActiveServiceCatalog() {
        CreateServiceCatalogRequest request = new CreateServiceCatalogRequest(TITLE);
        ServiceCatalog existingServiceCatalog = new ServiceCatalog(ID, TITLE, true);
        
        when(serviceCatalogRepository.findByTitle(TITLE)).thenReturn(Optional.of(existingServiceCatalog));
        
        ServiceCatalog result = serviceCatalogService.createServiceCatalog(request);
        
        assertNotNull(result);
        assertEquals(TITLE, result.getTitle());
        assertTrue(result.getActive());
        verify(serviceCatalogRepository, never()).save(any());
    }
    
    @Test
    void shouldDeactivateServiceCatalog() {
        ServiceCatalog serviceCatalog = new ServiceCatalog(ID, TITLE, true);
        
        when(serviceCatalogRepository.findById(ID)).thenReturn(Optional.of(serviceCatalog));
        when(serviceCatalogRepository.save(any(ServiceCatalog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        serviceCatalogService.deactivateServiceCatalog(ID);
        
        assertFalse(serviceCatalog.getActive());
        verify(serviceCatalogRepository).save(serviceCatalog);
    }
    
    @Test
    void shouldThrowExceptionWhenDeactivatingNonexistentServiceCatalog() {
        when(serviceCatalogRepository.findById(999)).thenReturn(Optional.empty());
        
        assertThrows(NotFoundException.class, () -> serviceCatalogService.deactivateServiceCatalog(999));
    }
}
