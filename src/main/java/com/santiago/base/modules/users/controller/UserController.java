package com.santiago.base.modules.users.controller;

import com.santiago.base.modules.users.dto.ResponseUserDTO;
import com.santiago.base.modules.users.dto.UpdateUserDTO;
import com.santiago.base.modules.users.dto.CreateUserDTO;
import com.santiago.base.modules.users.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Users entity routes")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<ResponseUserDTO>> findAll() {
        List<ResponseUserDTO> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseUserDTO> findById(@PathVariable Long id) {
        ResponseUserDTO user = userService.findById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseUserDTO> update(@PathVariable Long id, @Valid @RequestBody CreateUserDTO dto) {
        ResponseUserDTO updatedUser = userService.update(id, dto);
        return ResponseEntity.ok(updatedUser);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ResponseUserDTO> partialUpdate(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserDTO dto
    ) {
        ResponseUserDTO updatedUser = userService.partialUpdate(id, dto);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
