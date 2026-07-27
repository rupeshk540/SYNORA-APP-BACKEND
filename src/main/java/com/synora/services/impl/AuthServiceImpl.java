package com.synora.services.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.synora.dto.*;
import com.synora.entities.enums.AuthProvider;
import com.synora.entities.User;
import com.synora.repositories.UserRepository;
import com.synora.config.JwtTokenProvider;
import com.synora.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private GoogleOAuthService googleOAuthService;

    @Override
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(request.getDisplayName());
        user.setAuthProvider(AuthProvider.LOCAL);

        User saved = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(saved.getId(), saved.getEmail(), saved.getDisplayName());

        return new AuthResponse(token, saved.getEmail(), saved.getDisplayName());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getDisplayName());
        return new AuthResponse(token, user.getEmail(), user.getDisplayName());
    }

    @Override
    public AuthResponse loginWithGoogle(String credential) {
        GoogleIdToken.Payload payload = googleOAuthService.verify(credential);

        String email = payload.getEmail();
        String googleId = payload.getSubject();
        String name = (String) payload.get("name");

        User user = userRepository.findByEmail(email);

        if (user == null) {
            // no existing account at all - brand new user, arriving via Google
            user = new User();
            user.setEmail(email);
            user.setDisplayName(name != null ? name : email);
            user.setAuthProvider(AuthProvider.GOOGLE);
            user.setGoogleId(googleId);
            user = userRepository.save(user);
        } else if (user.getGoogleId() == null) {
            // existing local account, same email - link, don't duplicate
            user.setGoogleId(googleId);
            user = userRepository.save(user);
        }
        // if googleId is already set and matches, nothing to update - just issue a token

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getDisplayName());
        return new AuthResponse(token, user.getEmail(), user.getDisplayName());
    }
}
