package org.massage.parlor.service;

import lombok.RequiredArgsConstructor;
import org.massage.parlor.dto.UpdateUserRequest;
import org.massage.parlor.exception.NotFoundException;
import org.massage.parlor.model.Role;
import org.massage.parlor.model.Specialist;
import org.massage.parlor.model.User;
import org.massage.parlor.repository.SpecialistRepository;
import org.massage.parlor.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final SpecialistRepository specialistRepository;
    private final PasswordEncoder passwordEncoder;
    
    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    @Transactional
    public User updateUser(Integer id, UpdateUserRequest request) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    if (request.getName() != null) {
                        existingUser.setName(request.getName());
                    }
                    if (request.getPassword() != null) {
                        existingUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
                    }
                    if (request.getPhone() != null) {
                        existingUser.setPhone(request.getPhone());
                    }
                    if (request.getAdditionalInfo() != null) {
                        existingUser.setAdditionalInfo(request.getAdditionalInfo());
                    }
                    return userRepository.save(existingUser);
                })
                .orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));
    }
    
    @Transactional
    public User updateUserRole(Integer id, Role role) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    Role oldRole = existingUser.getRole();
                    existingUser.setRole(role);
                    User savedUser = userRepository.save(existingUser);
                    
                    if (oldRole != Role.SPECIALIST && role == Role.SPECIALIST) {
                        Optional<Specialist> existingSpecialist = specialistRepository.findById(id);
                        if (existingSpecialist.isPresent()) {
                            Specialist specialist = existingSpecialist.get();
                            specialist.setActive(true);
                            specialistRepository.save(specialist);
                        } else {
                            Specialist specialist = new Specialist();
                            specialist.setUser(savedUser);
                            specialist.setExperience(0);
                            specialistRepository.save(specialist);
                        }
                    } else if (oldRole == Role.SPECIALIST && role != Role.SPECIALIST) {
                        specialistRepository.findById(id).ifPresent(specialist -> {
                            specialist.setActive(false);
                            specialistRepository.save(specialist);
                        });
                    }
                    
                    return savedUser;
                })
                .orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));
    }
    
    public Optional<User> getUserByLogin(String login) {
        return userRepository.findByLogin(login);
    }
}
