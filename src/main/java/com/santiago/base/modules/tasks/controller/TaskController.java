package com.santiago.base.modules.tasks.controller;

import com.santiago.base.modules.tasks.dto.CreateTaskDTO;
import com.santiago.base.modules.tasks.dto.ResponseTaskDTO;
import com.santiago.base.modules.tasks.dto.UpdateTaskDTO;
import com.santiago.base.modules.tasks.service.TaskService;
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

    @GetMapping("/{id}")
    public ResponseEntity<ResponseTaskDTO> findById(@PathVariable Long id) {
        ResponseTaskDTO task = taskService.findById(id);
        return ResponseEntity.ok(task);
    }

    @PostMapping
    public ResponseEntity<CreateTaskDTO> create(@Valid @RequestBody CreateTaskDTO dto) {
        CreateTaskDTO newTask = taskService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newTask);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseTaskDTO> update(@PathVariable Long id, @Valid @RequestBody CreateTaskDTO dto) {
        ResponseTaskDTO updatedTask = taskService.update(id, dto);
        return ResponseEntity.ok(updatedTask);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ResponseTaskDTO> partialUpdate(@PathVariable Long id, @Valid @RequestBody UpdateTaskDTO dto) {
        ResponseTaskDTO updatedTask = taskService.partialUpdate(id, dto);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}