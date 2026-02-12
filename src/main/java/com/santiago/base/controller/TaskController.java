package com.santiago.base.controller;

import com.santiago.base.dto.TaskDTO;
import com.santiago.base.dto.UpdateTaskDTO;
import com.santiago.base.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<List<TaskDTO>> findAll(@RequestParam(required = false) Long userId) {
        List<TaskDTO> tasks;

        if (userId != null) {
            tasks = taskService.findByUserId(userId);
        } else {
            tasks = taskService.findAll();
        }

        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> findById(@PathVariable Long id) {
        TaskDTO task = taskService.findById(id);
        return ResponseEntity.ok(task);
    }

    @PostMapping
    public ResponseEntity<TaskDTO> create(@Valid @RequestBody TaskDTO dto) {
        TaskDTO newTask = taskService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newTask);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> update(@PathVariable Long id, @Valid @RequestBody TaskDTO dto) {
        TaskDTO updatedTask = taskService.update(id, dto);
        return ResponseEntity.ok(updatedTask);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TaskDTO> partialUpdate(@PathVariable Long id, @Valid @RequestBody UpdateTaskDTO dto) {
        TaskDTO updatedTask = taskService.partialUpdate(id, dto);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}