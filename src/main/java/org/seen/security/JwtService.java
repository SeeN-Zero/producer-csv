package org.seen.security;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JwtService {
    public String generateToken(String username) {
        return Jwt.upn(username).sign();
    }
}
