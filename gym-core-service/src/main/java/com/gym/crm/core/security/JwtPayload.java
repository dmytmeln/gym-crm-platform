package com.gym.crm.core.security;

import java.util.Date;

public record JwtPayload(
        String username,
        String jti,
        Date expiration
) {
}
