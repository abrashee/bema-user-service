package com.bema.bema_user_service.service.serviceImplementation;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.bema.bema_user_service.dto.user.UserCreateDto;
import com.bema.bema_user_service.dto.user.UserDto;
import com.bema.bema_user_service.dto.user.UserUpdateDto;
import com.bema.bema_user_service.entity.UserEntity;
import com.bema.bema_user_service.exception.InvalidUserException;
import com.bema.bema_user_service.exception.ResourceNotFoundException;
import com.bema.bema_user_service.repository.UserRepository;
import com.bema.bema_user_service.service.serviceInterface.UserService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UserServiceImplementation implements UserService {

    private final UserRepository userRepository;

    public UserServiceImplementation(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ─────────────────────────────────────────────
    // GET ALL USERS
    // ─────────────────────────────────────────────
    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // ─────────────────────────────────────────────
    // GET BY ID
    // ─────────────────────────────────────────────
    @Override
    public UserDto getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + id));
    }

    // ─────────────────────────────────────────────
    // GET BY IDENTITY ID
    // ─────────────────────────────────────────────
    @Override
    public UserDto getUserByIdentityId(String identityId) {
        return userRepository.findByIdentityId(identityId)
                .map(this::mapToDto)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with identityId: " + identityId));
    }

    // ─────────────────────────────────────────────
    // CREATE USER PROFILE
    // ─────────────────────────────────────────────
    @Override
    public UserDto createUser(UserCreateDto dto) {

        Optional<UserEntity> existing =
                userRepository.findByIdentityId(dto.identityId());

        if (existing.isPresent()) {
            return mapToDto(existing.get());
        }

        UserEntity user = UserEntity.builder()
                .identityId(dto.identityId())
                .name(dto.name())
                .build();

        userRepository.save(user);

        return mapToDto(user);
    }

    // ─────────────────────────────────────────────
    // UPDATE USER PROFILE
    // ─────────────────────────────────────────────
    @Override
    public UserDto updateUser(Long id, UserUpdateDto dto) {

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + id));

        if (dto.name() != null) {
            user.setName(dto.name());
        }

        userRepository.save(user);

        return mapToDto(user);
    }

    // ─────────────────────────────────────────────
    // DELETE USER
    // ─────────────────────────────────────────────
    @Override
    public void deleteUser(Long id) {

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }

        userRepository.deleteById(id);
    }

    // ─────────────────────────────────────────────
    // MAPPER
    // ─────────────────────────────────────────────
    private UserDto mapToDto(UserEntity entity) {
        return new UserDto(
                entity.getId(),
                entity.getIdentityId(),
                entity.getName()
        );
    }
}