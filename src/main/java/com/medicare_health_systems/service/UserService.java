package com.medicare_health_systems.service;

import com.medicare_health_systems.dto.request.RegisterRequest;
import com.medicare_health_systems.dto.response.UserResponse;
import com.medicare_health_systems.entity.Role;
import com.medicare_health_systems.entity.User;
import com.medicare_health_systems.exceptions.ResourceNotFound;
import com.medicare_health_systems.exceptions.UserAlreadyExistsException;
import com.medicare_health_systems.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;


    public UserResponse getCurrentUser(){
        User currentUser = getAuthenticatedUser();
        return  mapToUserResponse(currentUser);
    }
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFound("User", "id", userId));
        return mapToUserResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(Role role) {
        return userRepository.findByRole(role)
                .stream()
                .map(this::mapToUserResponse)
                .toList();
    }
    //deactivate a user
    @Transactional
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFound("User", "id", userId));

        user.setIsActive(false);
        userRepository.save(user);
        log.info("User deactivated. ID: {}", userId);
    }
    public User getAuthenticatedUser() {
        return (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    @Transactional
    public UserResponse registerUser(RegisterRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new UserAlreadyExistsException("User already exists with the email : "+ request.getEmail());
        }
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(request.getPassword()) // plain for now
                .phone(request.getPhone())
                .role(request.getRole())
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered with ID: {}", savedUser.getId());
        return mapToUserResponse(savedUser);

    }

    private UserResponse mapToUserResponse(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

}
