package org.massage.parlor.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.massage.parlor.dto.UpdateSpecialistRequest;
import org.massage.parlor.exception.NotFoundException;
import org.massage.parlor.model.Specialist;
import org.massage.parlor.repository.SpecialistRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpecialistServiceTest {
    
    private static final Integer ID = 1;
    private static final Integer EXPERIENCE = 5;
    private static final Integer NONEXISTENT_ID = 999;
    
    @Mock
    private SpecialistRepository specialistRepository;
    
    @InjectMocks
    private SpecialistService specialistService;
    
    @Test
    void shouldUpdateSpecialist() {
        Specialist existingSpecialist = new Specialist();
        existingSpecialist.setId(ID);
        existingSpecialist.setExperience(EXPERIENCE);
        existingSpecialist.setActive(true);
        
        UpdateSpecialistRequest request = new UpdateSpecialistRequest(10);
        
        when(specialistRepository.findByIdAndActiveTrue(ID)).thenReturn(Optional.of(existingSpecialist));
        when(specialistRepository.save(any(Specialist.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        Specialist updatedSpecialist = specialistService.updateSpecialist(ID, request);
        
        assertEquals(10, updatedSpecialist.getExperience());
        verify(specialistRepository).save(existingSpecialist);
    }
    
    @Test
    void shouldThrowExceptionWhenUpdatingNonexistentSpecialist() {
        UpdateSpecialistRequest request = new UpdateSpecialistRequest(EXPERIENCE);
        
        when(specialistRepository.findByIdAndActiveTrue(NONEXISTENT_ID)).thenReturn(Optional.empty());
        
        assertThrows(NotFoundException.class, () -> specialistService.updateSpecialist(NONEXISTENT_ID, request));
    }
}
