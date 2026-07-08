package com.example.demo.service.impl;

import com.example.demo.config.JwtService;
import com.example.demo.dto.AuthRequestDto;
import com.example.demo.dto.AuthResponseDto;
import com.example.demo.dto.RegisterDto;
import com.example.demo.entity.CityUser;
import com.example.demo.enums.Role;
import com.example.demo.exception.BusinessValidationException;
import com.example.demo.repository.CityUserRepository;
import com.example.demo.service.AuthService;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final CityUserRepository cityUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public AuthResponseDto register(RegisterDto dto) {
        if (cityUserRepository.existsByUsername(dto.getUsername())) {
            throw new BusinessValidationException("Username already exists");
        }
        if (cityUserRepository.existsByBadgeNumber(dto.getBadgeNumber())) {
            throw new BusinessValidationException("Badge number already exists");
        }

        Role role = parseRole(dto.getRole());
        CityUser cityUser = CityUser.builder()
                .username(dto.getUsername())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .role(role)
                .fullName(dto.getFullName())
                .district(dto.getDistrict())
                .badgeNumber(dto.getBadgeNumber())
                .isActive(true)
                .build();

        CityUser saved = cityUserRepository.save(cityUser);
        String token = jwtService.generateToken(org.springframework.security.core.userdetails.User
                .withUsername(saved.getUsername())
                .password(saved.getPasswordHash())
                .authorities("ROLE_" + saved.getRole().name())
                .build());
        return new AuthResponseDto(token, saved.getUsername(), saved.getRole().name());
    }

    @Override
    public AuthResponseDto login(AuthRequestDto dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));

        CityUser user = cityUserRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new BusinessValidationException("Invalid username or password"));
        String token = jwtService.generateToken(org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .authorities("ROLE_" + user.getRole().name())
                .build());
        return new AuthResponseDto(token, user.getUsername(), user.getRole().name());
    }

    private Role parseRole(String role) {
        try {
            return Role.valueOf(role.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BusinessValidationException("Invalid role");
        }
    }
}
