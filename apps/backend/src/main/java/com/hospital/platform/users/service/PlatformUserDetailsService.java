package com.hospital.platform.users.service;

import com.hospital.platform.users.mapper.UserSecurityMapper;
import com.hospital.platform.users.repository.UserRepository;
import java.util.UUID;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlatformUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserSecurityMapper userSecurityMapper;

    public PlatformUserDetailsService(UserRepository userRepository, UserSecurityMapper userSecurityMapper) {
        this.userRepository = userRepository;
        this.userSecurityMapper = userSecurityMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthenticatedUser loadUserByUsername(String email) {
        return userRepository.findByEmailWithRolesAndPermissions(email)
                .map(userSecurityMapper::toAuthenticatedUser)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public AuthenticatedUser loadUserById(UUID userId) {
        return userRepository.findByIdWithRolesAndPermissions(userId)
                .map(userSecurityMapper::toAuthenticatedUser)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
