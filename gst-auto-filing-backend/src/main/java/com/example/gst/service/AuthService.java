package com.example.gst.service;

import com.example.gst.dto.AuthResponse;
import com.example.gst.dto.LoginRequest;
import com.example.gst.entity.User;
import com.example.gst.repository.UserRepository;
import com.example.gst.security.JwtUtil;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void initAdminUser() {
        if (userRepository.findByEmail("admin@gst.com").isEmpty()) {
            User admin = new User();
            admin.setName("Admin User");
            admin.setEmail("admin@gst.com");
            admin.setPassword(passwordEncoder.encode("password123"));
            userRepository.save(admin);
        }
    }

    public AuthResponse login(LoginRequest loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
        final String jwt = jwtUtil.generateToken(userDetails);
        
        User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow();

        return new AuthResponse(jwt, user.getName(), user.getEmail());
    }
}
