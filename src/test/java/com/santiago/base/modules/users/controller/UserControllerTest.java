package com.santiago.base.modules.users.controller;

import com.santiago.base.core.pagination.dto.PaginatedResponseDTO;
import com.santiago.base.core.pagination.dto.PaginationMetadataDTO;
import com.santiago.base.core.security.UserSessionModel;
import com.santiago.base.modules.users.dto.ResponseUserDTO;
import com.santiago.base.modules.users.dto.UpdateUserDTO;
import com.santiago.base.modules.users.dto.UserDTO;
import com.santiago.base.modules.users.entity.User;
import com.santiago.base.modules.users.model.UserRole;
import com.santiago.base.modules.users.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static com.santiago.base.support.TestFixtures.session;
import static com.santiago.base.support.TestFixtures.updateUserDto;
import static com.santiago.base.support.TestFixtures.user;
import static com.santiago.base.support.TestFixtures.userDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void findAllShouldReturnOkWithPaginatedUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        PaginatedResponseDTO<ResponseUserDTO> expected = paginatedUser(user(1L, UserRole.USER));
        when(userService.findAll(pageable)).thenReturn(expected);

        ResponseEntity<PaginatedResponseDTO<ResponseUserDTO>> response = userController.findAll(pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expected);
        verify(userService).findAll(pageable);
    }

    @Test
    void findByIdShouldReturnOkWithUser() {
        UserSessionModel requestUser = session(1L, UserRole.USER);
        ResponseUserDTO expected = new ResponseUserDTO(user(1L, UserRole.USER));
        when(userService.findById(1L, requestUser)).thenReturn(expected);

        ResponseEntity<ResponseUserDTO> response = userController.findById(1L, requestUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expected);
        verify(userService).findById(1L, requestUser);
    }

    @Test
    void updateShouldReturnOkWithUpdatedUser() {
        UserDTO dto = userDto("Updated", "updated@example.com");
        UserSessionModel requestUser = session(1L, UserRole.USER);
        ResponseUserDTO expected = new ResponseUserDTO(user(1L, UserRole.USER));
        when(userService.update(1L, dto, requestUser)).thenReturn(expected);

        ResponseEntity<ResponseUserDTO> response = userController.update(1L, dto, requestUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expected);
        verify(userService).update(1L, dto, requestUser);
    }

    @Test
    void partialUpdateShouldReturnOkWithUpdatedUser() {
        UpdateUserDTO dto = updateUserDto("Updated", null);
        UserSessionModel requestUser = session(1L, UserRole.USER);
        ResponseUserDTO expected = new ResponseUserDTO(user(1L, UserRole.USER));
        when(userService.partialUpdate(1L, dto, requestUser)).thenReturn(expected);

        ResponseEntity<ResponseUserDTO> response = userController.partialUpdate(1L, dto, requestUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expected);
        verify(userService).partialUpdate(1L, dto, requestUser);
    }

    @Test
    void activateShouldReturnOkWithActivatedUser() {
        UserSessionModel requestUser = session(99L, UserRole.ADMIN);
        ResponseUserDTO expected = new ResponseUserDTO(user(1L, UserRole.USER));
        when(userService.activate(1L, requestUser)).thenReturn(expected);

        ResponseEntity<ResponseUserDTO> response = userController.activate(1L, requestUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expected);
        verify(userService).activate(1L, requestUser);
    }

    @Test
    void deactivateShouldReturnOkWithDeactivatedUser() {
        UserSessionModel requestUser = session(1L, UserRole.USER);
        ResponseUserDTO expected = new ResponseUserDTO(user(1L, UserRole.USER));
        when(userService.deactivate(1L, requestUser)).thenReturn(expected);

        ResponseEntity<ResponseUserDTO> response = userController.deactivate(1L, requestUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expected);
        verify(userService).deactivate(1L, requestUser);
    }

    @Test
    void deleteShouldReturnNoContent() {
        ResponseEntity<Void> response = userController.delete(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(userService).delete(1L);
    }

    private static PaginatedResponseDTO<ResponseUserDTO> paginatedUser(User user) {
        return new PaginatedResponseDTO<>(
                List.of(new ResponseUserDTO(user)),
                new PaginationMetadataDTO(10, 0, 1, 1)
        );
    }
}
