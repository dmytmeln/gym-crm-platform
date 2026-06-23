package com.gym.crm.core.exception;

import static java.lang.String.format;

public class DownstreamServiceUnavailableException extends ServiceException {

    public DownstreamServiceUnavailableException(String serviceName, Throwable cause) {
        super(format("Service unavailable: %s request failed", serviceName), cause);
    }

}
