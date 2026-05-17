package com.edsonjr.taskflow.domain.service;

import com.edsonjr.taskflow.domain.model.AppUser;
import com.edsonjr.taskflow.domain.repository.AppUserRepository;
import com.edsonjr.taskflow.exception.EmailAlreadyExistsException;
import com.edsonjr.taskflow.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private AppUserService appUserService;

    @Test
    void shouldCreateAppUserWhenNameAndEmailAreValid() {
        UUID userId = UUID.randomUUID();

        when(appUserRepository.existsByEmailIgnoreCase("john.doe@email.com"))
                .thenReturn(false);

        when(appUserRepository.save(any(AppUser.class)))
                .thenAnswer(invocation -> {
                    AppUser appUser = invocation.getArgument(0);
                    ReflectionTestUtils.setField(appUser, "id", userId);
                    return appUser;
                });

        AppUser createdUser = appUserService.create("John Doe", "john.doe@email.com");

        assertThat(createdUser.getId()).isEqualTo(userId);
        assertThat(createdUser.getName()).isEqualTo("John Doe");
        assertThat(createdUser.getEmail()).isEqualTo("john.doe@email.com");

        ArgumentCaptor<AppUser> appUserCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(appUserCaptor.capture());

        AppUser savedUser = appUserCaptor.getValue();

        assertThat(savedUser.getName()).isEqualTo("John Doe");
        assertThat(savedUser.getEmail()).isEqualTo("john.doe@email.com");

        verify(appUserRepository).existsByEmailIgnoreCase("john.doe@email.com");
        verifyNoMoreInteractions(appUserRepository);
    }

    @Test
    void shouldThrowEmailAlreadyExistsExceptionWhenEmailAlreadyExists() {
        when(appUserRepository.existsByEmailIgnoreCase("john.doe@email.com"))
                .thenReturn(true);

        assertThatThrownBy(() -> appUserService.create("John Doe", "john.doe@email.com"))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Unable to create user with provided data.");

        verify(appUserRepository).existsByEmailIgnoreCase("john.doe@email.com");
        verify(appUserRepository, never()).save(any(AppUser.class));
        verifyNoMoreInteractions(appUserRepository);
    }

    @Test
    void shouldFindAppUserByExistingId() {
        UUID userId = UUID.randomUUID();
        AppUser appUser = AppUser.create("John Doe", "john.doe@email.com");
        ReflectionTestUtils.setField(appUser, "id", userId);

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(appUser));

        AppUser foundUser = appUserService.findById(userId);

        assertThat(foundUser.getId()).isEqualTo(userId);
        assertThat(foundUser.getName()).isEqualTo("John Doe");
        assertThat(foundUser.getEmail()).isEqualTo("john.doe@email.com");

        verify(appUserRepository).findById(userId);
        verifyNoMoreInteractions(appUserRepository);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenAppUserDoesNotExist() {
        UUID userId = UUID.randomUUID();

        when(appUserRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appUserService.findById(userId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User not found.");

        verify(appUserRepository).findById(userId);
        verifyNoMoreInteractions(appUserRepository);
    }
}