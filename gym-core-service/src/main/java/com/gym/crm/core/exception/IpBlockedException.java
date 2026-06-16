package com.gym.crm.core.exception;

public class IpBlockedException extends AuthenticationException {

    public IpBlockedException(String message) {
        super(message);
    }

    public IpBlockedException(String message, Throwable cause) {
        super(message, cause);
    }

}
