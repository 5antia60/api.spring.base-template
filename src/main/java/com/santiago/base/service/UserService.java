package com.santiago.base.service;

import com.santiago.base.dto.UserDTO;
import com.santiago.base.entity.User;
import com.santiago.base.exception.ResourceNotFoundException;
import com.santiago.base.exception.BusinessException;
import com.santiago.base.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserDTO> findAll() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com id: " + id));
        return convertToDTO(user);
    }

    @Transactional
    public UserDTO create(UserDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("Email já cadastrado: " + dto.getEmail());
        }

        User user = convertToEntity(dto);
        User salvo = userRepository.save(user);
        return convertToDTO(salvo);
    }

    @Transactional
    public UserDTO update(Long id, UserDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com id: " + id));

        if (!user.getEmail().equals(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("Email já cadastrado: " + dto.getEmail());
        }

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuário não encontrado com id: " + id);
        }

        userRepository.deleteById(id);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }

    private User convertToEntity(UserDTO dto) {
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        return user;
    }
}
