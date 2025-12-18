package com.distributed_system.purchasement.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Event for user-related operations
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserEvent extends BaseEvent {

    private Long userId;
    private String username;
    private String email;
    private int age;
    private String action; // CREATED, UPDATED, DELETED

    /**
     * Create a user created event
     */
    public static UserEvent created(Long userId, String username, String email, int age) {
        UserEvent event = UserEvent.builder()
                .userId(userId)
                .username(username)
                .email(email)
                .age(age)
                .action("CREATED")
                .build();
        event.initializeMetadata("USER_CREATED", "purchasement-service");
        return event;
    }

    /**
     * Create a user updated event
     */
    public static UserEvent updated(Long userId, String username, String email, int age) {
        UserEvent event = UserEvent.builder()
                .userId(userId)
                .username(username)
                .email(email)
                .age(age)
                .action("UPDATED")
                .build();
        event.initializeMetadata("USER_UPDATED", "purchasement-service");
        return event;
    }

    /**
     * Create a user deleted event
     */
    public static UserEvent deleted(Long userId) {
        UserEvent event = UserEvent.builder()
                .userId(userId)
                .action("DELETED")
                .build();
        event.initializeMetadata("USER_DELETED", "purchasement-service");
        return event;
    }
}

