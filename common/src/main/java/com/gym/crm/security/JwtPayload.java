package com.gym.crm.security;

import java.util.Date;

public record JwtPayload(
        String username,
        String jti,
        Date expiration
) {
}
