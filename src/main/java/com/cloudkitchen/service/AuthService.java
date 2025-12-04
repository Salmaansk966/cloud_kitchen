package com.cloudkitchen.service;

import com.cloudkitchen.dto.auth.JwtResponse;
import com.cloudkitchen.dto.auth.LoginRequest;
import com.cloudkitchen.dto.auth.RegisterRequest;

public interface AuthService {

    JwtResponse register(RegisterRequest request);

    JwtResponse login(LoginRequest request);
}





