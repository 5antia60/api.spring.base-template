package com.santiago.base.modules.tasks.controller;

import com.santiago.base.modules.tasks.dto.CreateTaskDTO;
import com.santiago.base.modules.tasks.dto.ResponseTaskDTO;
import com.santiago.base.modules.tasks.dto.UpdateTaskDTO;
import com.santiago.base.modules.tasks.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Tasks entity routes")
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Busca todas as tarefas", description = "Retorna lista paginada de tarefas")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    @GetMapping
    public ResponseEntity<List<ResponseTaskDTO>> findAll(@RequestParam(required = false) Long userId) {
        List<ResponseTaskDTO> tasks;

        if (userId != null) {
            tasks = taskService.findByUserId(userId);
        } else {
            tasks = taskService.findAll();
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

    @Operation(summary = "Deleta a tarefa por Id", description = "Deleta a tarefa")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}