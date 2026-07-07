package com.bema.bema_user_service.service.serviceInterface;

import java.util.List;

import com.bema.bema_user_service.dto.user.UserCreateDto;
import com.bema.bema_user_service.dto.user.UserDto;
import com.bema.bema_user_service.dto.user.UserUpdateDto;

public interface UserService {

    List<UserDto> getAllUsers();

    UserDto getUserById(Long id);

    UserDto getUserByIdentityId(String identityId);

    UserDto createUser(UserCreateDto userCreateDto);

    UserDto updateUser(Long id, UserUpdateDto userUpdateDto);

    void deleteUser(Long id);
}