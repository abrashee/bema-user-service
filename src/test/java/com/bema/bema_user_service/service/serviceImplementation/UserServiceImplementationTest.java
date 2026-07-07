package com.bema.bema_user_service.service.serviceImplementation;

import com.bema.bema_user_service.dto.user.UserCreateDto;
import com.bema.bema_user_service.exception.ResourceNotFoundException;
import com.bema.bema_user_service.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:user_service_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "security.jwt.secret=bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
})
@Transactional
class UserServiceImplementationTest {

    @Autowired
    private UserServiceImplementation userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createUserReturnsExistingRecordWhenIdentityAlreadyExists() {
        userRepository.save(
                com.bema.bema_user_service.entity.UserEntity.builder()
                        .identityId("identity-1")
                        .name("Existing User")
                        .build()
        );

        var result = userService.createUser(new UserCreateDto("identity-1", "Existing User"));

        assertEquals("identity-1", result.identityId());
        assertEquals(1, userRepository.count());
    }

    @Test
    void getUserByIdentityIdThrowsWhenMissing() {
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserByIdentityId("missing"));
    }

    @Test
    void createUserPersistsNewProfile() {
        var result = userService.createUser(new UserCreateDto("identity-2", "New User"));

        assertEquals("identity-2", result.identityId());
        assertEquals(1, userRepository.count());
    }
}
