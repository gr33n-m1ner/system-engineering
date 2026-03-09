package org.massage.parlor.service;

import lombok.RequiredArgsConstructor;
import org.massage.parlor.dto.UpdateSpecialistRequest;
import org.massage.parlor.exception.NotFoundException;
import org.massage.parlor.model.Specialist;
import org.massage.parlor.repository.SpecialistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SpecialistService {
    
    private final SpecialistRepository specialistRepository;
    
    public Optional<Specialist> getSpecialistById(Integer id) {
        return specialistRepository.findByIdAndActiveTrue(id);
    }
    
    public List<Specialist> getAllSpecialists() {
        return specialistRepository.findAllByActiveTrue();
    }
    
    @Transactional
    public Specialist updateSpecialist(Integer id, UpdateSpecialistRequest request) {
        return specialistRepository.findByIdAndActiveTrue(id)
                .map(specialist -> {
                    if (request.getExperience() != null) {
                        specialist.setExperience(request.getExperience());
                    }
                    return specialistRepository.save(specialist);
                })
                .orElseThrow(() -> new NotFoundException("Specialist with id " + id + " not found"));
    }
}
