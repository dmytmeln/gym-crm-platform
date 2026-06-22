package com.gym.crm.workload.security;

import java.util.Date;

public record JwtPayload(
        String username,
        String jti,
        Date expiration
) {
}
