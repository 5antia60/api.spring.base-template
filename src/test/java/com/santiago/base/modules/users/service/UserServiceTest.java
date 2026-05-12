package com.santiago.base.modules.users.service;

import com.santiago.base.core.exceptions.BusinessException;
import com.santiago.base.core.exceptions.ResourceNotFoundException;
import com.santiago.base.core.pagination.dto.PaginatedResponseDTO;
import com.santiago.base.core.pagination.dto.PaginationMetadataDTO;
import com.santiago.base.core.pagination.service.PaginationService;
import com.santiago.base.modules.users.dto.ResponseUserDTO;
import com.santiago.base.modules.users.dto.UpdateUserDTO;
import com.santiago.base.modules.users.dto.UserDTO;
import com.santiago.base.modules.users.entity.User;
import com.santiago.base.modules.users.model.UserRole;
import com.santiago.base.modules.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.santiago.base.support.TestFixtures.session;
import static com.santiago.base.support.TestFixtures.updateUserDto;
import static com.santiago.base.support.TestFixtures.user;
import static com.santiago.base.support.TestFixtures.userDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "rawtypes"})
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaginationService paginationService;

    @InjectMocks
    private UserService userService;

    @Test
    void findAllShouldDelegatePaginationBuild() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(user(1L, UserRole.USER)), pageable, 1);
        PaginatedResponseDTO<ResponseUserDTO> expected = paginatedUserResponse();
        when(userRepository.findAll(pageable)).thenReturn(page);
        doReturn(expected).when(paginationService).build(eq(page), any(Function.class));

        PaginatedResponseDTO<ResponseUserDTO> response = userService.findAll(pageable);

        assertThat(response).isSameAs(expected);
        verify(userRepository).findAll(pageable);
        verify(paginationService).build(eq(page), any(Function.class));
    }

    @Test
    void findByIdShouldReturnUserWhenRequestUserIsOwner() {
        User user = user(1L, UserRole.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ResponseUserDTO response = userService.findById(1L, session(1L, UserRole.USER));

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void findByIdShouldReturnUserWhenRequestUserIsAdmin() {
        User user = user(1L, UserRole.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ResponseUserDTO response = userService.findById(1L, session(99L, UserRole.ADMIN));

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void findByIdShouldDenyWhenRequestUserIsNotOwnerOrAdmin() {
        assertThatThrownBy(() -> userService.findById(1L, session(2L, UserRole.USER)))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(userRepository);
    }

    @Test
    void findByIdShouldThrowWhenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(1L, session(1L, UserRole.USER)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1");
    }

    @Test
    void updateShouldUpdateNameAndEmailWhenEmailIsAvailable() {
        User existing = user(1L, UserRole.USER);
        UserDTO dto = userDto("Updated User", "updated@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.save(existing)).thenReturn(existing);

        ResponseUserDTO response = userService.update(1L, dto, session(1L, UserRole.USER));

        assertThat(response.getName()).isEqualTo("Updated User");
        assertThat(response.getEmail()).isEqualTo("updated@example.com");
        verify(userRepository).save(existing);
    }

    @Test
    void updateShouldNotCheckEmailUniquenessWhenEmailDoesNotChange() {
        User existing = user(1L, UserRole.USER);
        UserDTO dto = userDto("Updated User", existing.getEmail());
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        userService.update(1L, dto, session(1L, UserRole.USER));

        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository).save(existing);
    }

    @Test
    void updateShouldDenyWhenRequestUserIsNotOwnerOrAdmin() {
        UserDTO dto = userDto("Updated User", "updated@example.com");

        assertThatThrownBy(() -> userService.update(1L, dto, session(2L, UserRole.USER)))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(userRepository);
    }

    @Test
    void updateShouldThrowWhenUserDoesNotExist() {
        UserDTO dto = userDto("Updated User", "updated@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(1L, dto, session(1L, UserRole.USER)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateShouldThrowWhenEmailAlreadyExists() {
        User existing = user(1L, UserRole.USER);
        UserDTO dto = userDto("Updated User", "duplicated@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("duplicated@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.update(1L, dto, session(1L, UserRole.USER)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("duplicated@example.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    void partialUpdateShouldApplyOnlyPresentFields() {
        User existing = user(1L, UserRole.USER);
        String originalEmail = existing.getEmail();
        UpdateUserDTO dto = updateUserDto("Partial User", null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        ResponseUserDTO response = userService.partialUpdate(1L, dto, session(1L, UserRole.USER));

        assertThat(response.getName()).isEqualTo("Partial User");
        assertThat(response.getEmail()).isEqualTo(originalEmail);
        verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    void partialUpdateShouldUpdateEmailWhenAvailable() {
        User existing = user(1L, UserRole.USER);
        UpdateUserDTO dto = updateUserDto(null, "partial@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("partial@example.com")).thenReturn(false);
        when(userRepository.save(existing)).thenReturn(existing);

        ResponseUserDTO response = userService.partialUpdate(1L, dto, session(1L, UserRole.USER));

        assertThat(response.getEmail()).isEqualTo("partial@example.com");
    }

    @Test
    void partialUpdateShouldThrowWhenEmailAlreadyExists() {
        User existing = user(1L, UserRole.USER);
        UpdateUserDTO dto = updateUserDto(null, "duplicated@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("duplicated@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.partialUpdate(1L, dto, session(1L, UserRole.USER)))
                .isInstanceOf(BusinessException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void partialUpdateShouldDenyWhenRequestUserIsNotOwnerOrAdmin() {
        UpdateUserDTO dto = updateUserDto("Partial User", null);

        assertThatThrownBy(() -> userService.partialUpdate(1L, dto, session(2L, UserRole.USER)))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(userRepository);
    }

    @Test
    void activateShouldActivateUserWhenRequestUserIsAdmin() {
        User existing = user(1L, UserRole.USER);
        existing.setIsActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        ResponseUserDTO response = userService.activate(1L, session(99L, UserRole.ADMIN));

        assertThat(response.getIsActive()).isTrue();
        verify(userRepository).save(existing);
    }

    @Test
    void activateShouldDenyWhenRequestUserIsNotAdmin() {
        assertThatThrownBy(() -> userService.activate(1L, session(1L, UserRole.USER)))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(userRepository);
    }

    @Test
    void deactivateShouldDeactivateOwnUser() {
        User existing = user(1L, UserRole.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        ResponseUserDTO response = userService.deactivate(1L, session(1L, UserRole.USER));

        assertThat(response.getIsActive()).isFalse();
        verify(userRepository).save(existing);
    }

    @Test
    void deactivateShouldDenyWhenRequestUserIsNotOwnerOrAdmin() {
        assertThatThrownBy(() -> userService.deactivate(1L, session(2L, UserRole.USER)))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(userRepository);
    }

    @Test
    void deleteShouldDeleteExistingUser() {
        User existing = user(1L, UserRole.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));

        userService.delete(1L);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).delete(userCaptor.capture());
        assertThat(userCaptor.getValue()).isSameAs(existing);
    }

    @Test
    void deleteShouldThrowWhenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private static PaginatedResponseDTO<ResponseUserDTO> paginatedUserResponse() {
        return new PaginatedResponseDTO<>(
                List.of(new ResponseUserDTO(user(1L, UserRole.USER))),
                new PaginationMetadataDTO(10, 0, 1, 1)
        );
    }
}
