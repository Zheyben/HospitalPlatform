package com.hospital.platform.users.repository;

import com.hospital.platform.users.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("""
            select distinct u
            from User u
            left join fetch u.roles
            where u.deletedAt is null
            order by u.createdAt desc
            """)
    List<User> findAllActiveWithRoles();

    @Query("""
            select distinct u
            from User u
            left join fetch u.roles r
            left join fetch r.permissions
            where lower(u.email) = lower(:email)
            """)
    Optional<User> findByEmailWithRolesAndPermissions(@Param("email") String email);

    @Query("""
            select distinct u
            from User u
            left join fetch u.roles r
            left join fetch r.permissions
            where u.id = :id
            """)
    Optional<User> findByIdWithRolesAndPermissions(@Param("id") UUID id);

    @Query("""
            select distinct u
            from User u
            left join fetch u.roles
            where u.id = :id
              and u.deletedAt is null
            """)
    Optional<User> findActiveByIdWithRoles(@Param("id") UUID id);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCaseAndIdNot(String username, UUID id);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);
}
