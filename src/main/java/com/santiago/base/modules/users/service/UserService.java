package com.santiago.base.modules.users.service;

import com.santiago.base.core.exceptions.BusinessException;
import com.santiago.base.core.exceptions.ResourceNotFoundException;
import com.santiago.base.core.pagination.dto.PaginatedResponseDTO;
import com.santiago.base.core.pagination.service.PaginationService;
import com.santiago.base.core.security.UserSessionModel;
import com.santiago.base.modules.users.dto.UserDTO;
import com.santiago.base.modules.users.dto.ResponseUserDTO;
import com.santiago.base.modules.users.dto.UpdateUserDTO;
import com.santiago.base.modules.users.entity.User;
import com.santiago.base.modules.users.model.UserRole;
import com.santiago.base.modules.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PaginationService paginationService;

    @Transactional(readOnly = true)
    public PaginatedResponseDTO<ResponseUserDTO> findAll(Pageable pageable) {
        return paginationService.build(
                userRepository.findAll(pageable),
                this::convertToResponseUserDTO
        );
    }

    @Transactional(readOnly = true)
    public ResponseUserDTO findById(Long id, UserSessionModel requestUser) {
        if (!requestUser.getId().equals(id) && requestUser.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("user.accessDenied.viewOthers");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("user.notFound", id));

        return convertToResponseUserDTO(user);
    }

    @Transactional
    public ResponseUserDTO update(Long id, UserDTO dto, UserSessionModel requestUser) {
        if (!requestUser.getId().equals(id) && !requestUser.getRole().equals(UserRole.ADMIN)) {
            throw new AccessDeniedException("user.accessDenied.updateOthers");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("user.notFound", id));

        if (!user.getEmail().equals(dto.email()) && userRepository.existsByEmail(dto.email())) {
            throw new BusinessException("user.email.alreadyExists", dto.email());
        }

        user.setName(dto.name());
        user.setEmail(dto.email());

        User updatedUser = userRepository.save(user);
        return convertToResponseUserDTO(updatedUser);
    }

    @Transactional
    public ResponseUserDTO partialUpdate(Long id, UpdateUserDTO dto, UserSessionModel requestUser) {
        if (!requestUser.getId().equals(id) && requestUser.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("user.accessDenied.updateOthers");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("user.notFound", id));

        if (dto.getName() != null) {
            user.setName(dto.getName());
        }

        if (dto.getEmail() != null) {
            if (!user.getEmail().equals(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
                throw new BusinessException("user.email.alreadyExists", dto.getEmail());
            }

            user.setEmail(dto.getEmail());
        }

        User updatedUser = userRepository.save(user);
        return convertToResponseUserDTO(updatedUser);
    }

    @Transactional
    public ResponseUserDTO activate(Long id, UserSessionModel requestUser) {
        if (requestUser.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("user.accessDenied.activate");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("user.notFound", id));

        user.setIsActive(true);
        User updatedUser = userRepository.save(user);
        return convertToResponseUserDTO(updatedUser);
    }

    @Transactional
    public ResponseUserDTO deactivate(Long id, UserSessionModel requestUser) {
        if (!requestUser.getId().equals(id) && requestUser.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("user.accessDenied.deactivateOthers");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("user.notFound", id));

        user.setIsActive(false);
        User updatedUser = userRepository.save(user);
        return convertToResponseUserDTO(updatedUser);
    }

    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("user.notFound", id));

        userRepository.delete(user);
    }

    private ResponseUserDTO convertToResponseUserDTO(User user) {
        return new ResponseUserDTO(user);
    }
}
