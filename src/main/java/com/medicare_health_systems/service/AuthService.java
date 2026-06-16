package com.medicare_health_systems.service;

import com.medicare_health_systems.dto.request.LoginRequest;
import com.medicare_health_systems.dto.request.RegisterRequest;
import com.medicare_health_systems.dto.response.AuthResponse;
import com.medicare_health_systems.entity.User;
import com.medicare_health_systems.exceptions.UserAlreadyExistsException;
import com.medicare_health_systems.repository.UserRepository;
import com.medicare_health_systems.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final TokenInvalidateService tokenInvalidateService;

    @Transactional
    public AuthResponse register(RegisterRequest request){
        log.info("Registering new user with email : {}",request.getEmail());

        if(userRepository.existsByEmail(request.getEmail())){
            throw new UserAlreadyExistsException(request.getEmail());
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword())) //encode password and store
                .phone(request.getPhone())
                .role(request.getRole())
                .isActive(true)
                .build();
        User savedUser = userRepository.save(user);
        String jwtToken = jwtUtil.generateToken(savedUser);
        return buildAuthResponse(savedUser, jwtToken);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request){

        log.info("Login Attempt for user : {}", request.getEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));
        String jwtToken = jwtUtil.generateToken(user);
        log.info("User Logged in successfully! ID : {} Role : {}", user.getId(),user.getRole());

        return buildAuthResponse(user, jwtToken);

    }
    //logout function
    public void logout(String token){
        long remainingValidity = jwtUtil.getRemainingValidity(token);
        if(remainingValidity > 0){
            tokenInvalidateService.blacklistToken(token, remainingValidity);
            log.info("Token blacklisted on logout, remaining validity: {}ms", remainingValidity);
        }
    }


    private AuthResponse buildAuthResponse(User user, String jwtToken) {
        return AuthResponse.builder()
                .token(jwtToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationTime())
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .build();
    }
}
