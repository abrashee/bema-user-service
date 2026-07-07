package com.bema.bema_user_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bema.bema_user_service.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByIdentityId(String identityId);

    boolean existsByIdentityId(String identityId);
}