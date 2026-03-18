package com.santiago.base.modules.users.controller;

import com.santiago.base.core.pagination.dto.PaginatedResponseDTO;
import com.santiago.base.core.security.UserSessionModel;
import com.santiago.base.modules.users.dto.CreateUserDTO;
import com.santiago.base.modules.users.dto.ResponseUserDTO;
import com.santiago.base.modules.users.dto.UpdateUserDTO;
import com.santiago.base.modules.users.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Users entity routes")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Busca todos os usuários", description = "Retorna lista paginada de usuários")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    @GetMapping
    public ResponseEntity<PaginatedResponseDTO<ResponseUserDTO>> findAll(
            @ParameterObject
            @PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable
    ) {
        PaginatedResponseDTO<ResponseUserDTO> users = userService.findAll(pageable);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Busca o usuário por Id", description = "Retorna o usuário correspondente ao Id informado")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseUserDTO> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserSessionModel requestUser
    ) {
        ResponseUserDTO user = userService.findById(id, requestUser);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Atualiza o usuário por Id", description = "Atualiza e retorna o usuário atualizado")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    @PutMapping("/{id}")
    public ResponseEntity<ResponseUserDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateUserDTO dto,
            @AuthenticationPrincipal UserSessionModel requestUser
    ) {
        ResponseUserDTO updatedUser = userService.update(id, dto, requestUser);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Atualiza parcialmente o usuário por Id", description = "Atualiza parcialmente e retorna o usuário atualizado")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    @PatchMapping("/{id}")
    public ResponseEntity<ResponseUserDTO> partialUpdate(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserDTO dto,
            @AuthenticationPrincipal UserSessionModel requestUser
    ) {
        ResponseUserDTO updatedUser = userService.partialUpdate(id, dto, requestUser);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Deleta o usuário", description = "Deleta o usuário por Id")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}