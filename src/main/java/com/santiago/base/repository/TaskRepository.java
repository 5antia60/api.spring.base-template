package com.santiago.base.repository;

import com.santiago.base.model.TaskStatus;
import com.santiago.base.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUserId(Long userId);

    List<Task> findByStatus(TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.user.id = :userId AND t.status = :status")
    List<Task> findByUserIdAndStatus(Long userId, TaskStatus status);
}
