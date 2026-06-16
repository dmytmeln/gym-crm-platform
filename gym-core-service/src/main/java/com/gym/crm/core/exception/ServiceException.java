package com.gym.crm.core.exception;

public abstract class ServiceException extends RuntimeException {

    protected ServiceException(String message) {
        super(message);
    }

    protected ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
