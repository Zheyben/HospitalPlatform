package com.hospital.platform.users.service;

import java.util.UUID;

public interface UserLookupService {

    boolean existsActiveUser(UUID userId);
}
