package com.gym.crm.core.exception;

public class UserDeactivatedException extends AuthenticationException {

    public UserDeactivatedException(String message) {
        super(message);
    }

    public UserDeactivatedException(String message, Throwable cause) {
        super(message, cause);
    }

}
