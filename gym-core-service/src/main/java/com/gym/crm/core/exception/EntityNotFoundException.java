package com.gym.crm.core.exception;

import com.gym.crm.core.entity.EntityType;
import lombok.Getter;

@Getter
public class EntityNotFoundException extends ServiceException {

    private static final String ERROR_MESSAGE_TEMPLATE = "%s not found with %s: %s";

    public EntityNotFoundException(String message) {
        super(message);
    }

    public static EntityNotFoundException forUsername(EntityType entityType, String username) {
        return new EntityNotFoundException(buildErrorMessage(entityType, "username", username));
    }

    public static EntityNotFoundException forName(EntityType entityType, String name) {
        return new EntityNotFoundException(buildErrorMessage(entityType, "name", name));
    }

    private static String buildErrorMessage(EntityType entityType, String key, Object value) {
        return String.format(ERROR_MESSAGE_TEMPLATE, entityType.getName(), key, value);
    }

}
