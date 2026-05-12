package com.santiago.base.modules.tasks.entity;

import com.santiago.base.modules.tasks.model.TaskStatus;
import com.santiago.base.modules.users.entity.User;
import org.junit.jupiter.api.Test;

import static com.santiago.base.support.TestFixtures.user;
import static com.santiago.base.modules.users.model.UserRole.USER;
import static org.assertj.core.api.Assertions.assertThat;

class TaskTest {

    @Test
    void newTaskShouldStartActiveAndPending() {
        Task task = new Task();

        assertThat(task.getIsActive()).isTrue();
        assertThat(task.getStatus()).isEqualTo(TaskStatus.PENDING);
    }

    @Test
    void shouldStoreTaskFieldsAndOwner() {
        User user = user(1L, USER);
        Task task = new Task();

        task.setId(10L);
        task.setTitle("Implement tests");
        task.setDescription("Cover task entity");
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setUser(user);

        assertThat(task.getId()).isEqualTo(10L);
        assertThat(task.getTitle()).isEqualTo("Implement tests");
        assertThat(task.getDescription()).isEqualTo("Cover task entity");
        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(task.getUser()).isSameAs(user);
    }
}
