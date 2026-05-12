package com.santiago.base.modules.tasks.repository;

import com.santiago.base.modules.tasks.entity.Task;
import com.santiago.base.modules.tasks.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @EntityGraph(attributePaths = {"user"})
    Page<Task> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    Page<Task> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    Optional<Task> findById(Long id);

    @EntityGraph(attributePaths = {"user"})
    List<Task> findByStatus(TaskStatus status);

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT t FROM Task t WHERE t.user.id = :userId AND t.status = :status")
    List<Task> findByUserIdAndStatus(Long userId, TaskStatus status);
}
