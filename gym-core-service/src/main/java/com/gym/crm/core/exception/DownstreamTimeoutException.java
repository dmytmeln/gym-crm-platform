package com.gym.crm.core.exception;

import static java.lang.String.format;

public class DownstreamTimeoutException extends ServiceException {

    public DownstreamTimeoutException(String serviceName, int timeoutSeconds, Throwable cause) {
        super(format("Timeout: %s did not respond within %ds", serviceName, timeoutSeconds), cause);
    }

}
