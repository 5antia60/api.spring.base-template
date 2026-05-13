package com.santiago.base.core.event;

import com.santiago.base.modules.tasks.entity.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EventListenerService {

    @EventListener
    public void onDomainEvent(DomainEvent event) {
        if (event.type() instanceof EventType eventType) {
            switch (eventType) {
                case UPDATED_TASK -> onTaskUpdated((Task) event.payload());
                case DELETED_TASK -> onTaskDeleted((Task) event.payload());
            }
        }
    }

    private void onTaskUpdated(Task task) {
        System.out.println("Task updated for user: " + task.getUser().getName());
    }

    private void onTaskDeleted(Task task) {
        System.out.println("Task deleted for user: " + task.getUser().getName());
    }
}
