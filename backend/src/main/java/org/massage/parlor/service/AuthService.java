package org.massage.parlor.service;

import lombok.RequiredArgsConstructor;
import org.massage.parlor.dto.AuthResponse;
import org.massage.parlor.dto.LoginRequest;
import org.massage.parlor.dto.RegisterRequest;
import org.massage.parlor.model.Role;
import org.massage.parlor.model.User;
import org.massage.parlor.repository.UserRepository;
import org.massage.parlor.security.JwtUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        User user = new User(
                null,
                request.getLogin(),
                passwordEncoder.encode(request.getPassword()),
                request.getName(),
                Role.CLIENT,
                request.getPhone(),
                null,
                null
        );
        
        User savedUser = userRepository.save(user);
        
        String token = jwtUtil.generateToken(savedUser.getId(), savedUser.getRole());
        
        return new AuthResponse(token, savedUser.getLogin(), savedUser.getRole());
    }
    
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByLogin(request.getLogin())
                .orElseThrow(() -> new BadCredentialsException("Invalid login or password"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid login or password");
        }
        
        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        
        return new AuthResponse(token, user.getLogin(), user.getRole());
    }
}
