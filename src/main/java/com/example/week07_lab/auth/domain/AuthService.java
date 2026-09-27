package com.example.week07_lab.auth.domain;

import com.example.week07_lab.auth.dto.LoginRequest;
import com.example.week07_lab.auth.dto.TokenResponse;
import com.example.week07_lab.auth.infrastructure.JwtService;
import com.example.week07_lab.shared.exception.NotFoundException;
import com.example.week07_lab.shared.exception.UnauthorizedException;
import com.example.week07_lab.user.domain.User;
import com.example.week07_lab.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        User found = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new NotFoundException("No existe un usuario con ese email"));

        boolean passwordOk = passwordEncoder.matches(request.getPassword(), found.getPassword());
        if (!passwordOk) {
            throw new UnauthorizedException("Contrasena incorrecta");
        }

        String jwt = jwtService.generateToken(found);
        return new TokenResponse(jwt);
    }
}
