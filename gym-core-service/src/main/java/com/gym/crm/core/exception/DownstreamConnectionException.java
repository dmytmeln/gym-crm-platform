package com.gym.crm.core.exception;

import static java.lang.String.format;

public class DownstreamConnectionException extends ServiceException {

    public DownstreamConnectionException(String serviceName, Throwable cause) {
        super(format("Connection error: Cannot connect to %s", serviceName), cause);
    }

}
