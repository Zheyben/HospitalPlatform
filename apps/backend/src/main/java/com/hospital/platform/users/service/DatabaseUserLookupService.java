package com.hospital.platform.users.service;

import com.hospital.platform.users.repository.UserRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class DatabaseUserLookupService implements UserLookupService {

    private final UserRepository userRepository;

    DatabaseUserLookupService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsActiveUser(UUID userId) {
        return userRepository.findActiveByIdWithRoles(userId).isPresent();
    }
}
