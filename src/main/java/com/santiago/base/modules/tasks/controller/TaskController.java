package com.santiago.base.modules.tasks.controller;

import com.santiago.base.core.pagination.dto.PaginatedResponseDTO;
import com.santiago.base.core.security.UserSessionModel;
import com.santiago.base.modules.tasks.dto.CreateTaskDTO;
import com.santiago.base.modules.tasks.dto.ResponseTaskDTO;
import com.santiago.base.modules.tasks.dto.UpdateTaskDTO;
import com.santiago.base.modules.tasks.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Tasks entity routes")
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Busca todas as tarefas", description = "Retorna lista paginada de tarefas")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    @GetMapping
    public ResponseEntity<PaginatedResponseDTO<ResponseTaskDTO>> findAll(
            @RequestParam(required = false) Long userId,
            @ParameterObject
            @PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable
    ) {
        PaginatedResponseDTO<ResponseTaskDTO> tasks;

        if (userId != null) {
            tasks = taskService.findByUserId(userId, pageable);
        } else {
            tasks = taskService.findAll(pageable);
        }

        return ResponseEntity.ok(tasks);
    }

    @Operation(summary = "Busca a tarefa por Id", description = "Retorna a tarefa correspondente ao Id informado")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseTaskDTO> findById(@PathVariable Long id) {
        ResponseTaskDTO task = taskService.findById(id);
        return ResponseEntity.ok(task);
    }

    @Operation(summary = "Criar tarefa", description = "Cria uma nova tarefa")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    @PostMapping
    public ResponseEntity<CreateTaskDTO> create(@Valid @RequestBody CreateTaskDTO dto) {
        CreateTaskDTO newTask = taskService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newTask);
    }

    @Operation(summary = "Atualiza a tarefa por Id", description = "Atualiza e retorna a tarefa atualizada")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    @PutMapping("/{id}")
    public ResponseEntity<ResponseTaskDTO> update(@PathVariable Long id, @Valid @RequestBody CreateTaskDTO dto) {
        ResponseTaskDTO updatedTask = taskService.update(id, dto);
        return ResponseEntity.ok(updatedTask);
    }

    @Operation(summary = "Atualiza parcialmente a tarefa por Id", description = "Atualiza parcialmente e retorna a tarefa atualizada")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    @PatchMapping("/{id}")
    public ResponseEntity<ResponseTaskDTO> partialUpdate(@PathVariable Long id, @Valid @RequestBody UpdateTaskDTO dto) {
        ResponseTaskDTO updatedTask = taskService.partialUpdate(id, dto);
        return ResponseEntity.ok(updatedTask);
    }

    @Operation(summary = "Ativa a tarefa por Id", description = "Ativa e retorna a tarefa atualizada")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/activate/{id}")
    public ResponseEntity<ResponseTaskDTO> activate(
            @PathVariable Long id,
            @AuthenticationPrincipal UserSessionModel requestUser
    ) {
        ResponseTaskDTO updatedTask = taskService.activate(id, requestUser);
        return ResponseEntity.ok(updatedTask);
    }

    @Operation(summary = "Desativa a tarefa por Id", description = "Desativa e retorna a tarefa atualizada")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/deactivate/{id}")
    public ResponseEntity<ResponseTaskDTO> deactivate(
            @PathVariable Long id,
            @AuthenticationPrincipal UserSessionModel requestUser
    ) {
        ResponseTaskDTO updatedTask = taskService.deactivate(id, requestUser);
        return ResponseEntity.ok(updatedTask);
    }

    @Operation(summary = "Deleta a tarefa por Id", description = "Deleta a tarefa")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}