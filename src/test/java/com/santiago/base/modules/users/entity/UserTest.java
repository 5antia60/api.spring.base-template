package com.santiago.base.modules.users.entity;

import com.santiago.base.modules.tasks.entity.Task;
import com.santiago.base.modules.users.model.UserRole;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void newUserShouldStartActiveWithUserRoleAndEmptyTasks() {
        User user = new User();

        assertThat(user.getIsActive()).isTrue();
        assertThat(user.getRole()).isEqualTo(UserRole.USER);
        assertThat(user.getTasks()).isEmpty();
    }

    @Test
    void shouldStoreBasicFieldsAndTasks() {
        User user = new User();
        Task task = new Task();

        user.setId(1L);
        user.setName("Santiago");
        user.setEmail("santiago@example.com");
        user.setPassword("secret");
        user.setRole(UserRole.ADMIN);
        user.getTasks().add(task);

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("Santiago");
        assertThat(user.getEmail()).isEqualTo("santiago@example.com");
        assertThat(user.getPassword()).isEqualTo("secret");
        assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(user.getTasks()).containsExactly(task);
    }
}
