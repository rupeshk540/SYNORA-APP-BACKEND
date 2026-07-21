package com.synora.services;

import com.synora.dto.AuthResponse;
import com.synora.dto.LoginRequest;
import com.synora.dto.SignupRequest;

public interface AuthService {

    AuthResponse signup(SignupRequest request);
    AuthResponse login(LoginRequest request);
}
