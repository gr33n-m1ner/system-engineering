package org.massage.parlor.repository;

import org.massage.parlor.model.Specialist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpecialistRepository extends JpaRepository<Specialist, Integer> {
    
    Optional<Specialist> findByIdAndActiveTrue(Integer id);
    
    List<Specialist> findAllByActiveTrue();
}
