package com.synora.services.impl;

import com.synora.entities.User;
import com.synora.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    @Autowired
    private UserRepository userRepository;

    public User getCurrentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName());
    }
}