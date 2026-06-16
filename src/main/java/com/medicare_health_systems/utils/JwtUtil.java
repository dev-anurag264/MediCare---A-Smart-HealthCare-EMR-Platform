package com.medicare_health_systems.utils;


import com.medicare_health_systems.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class JwtUtil {
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {

        if (userDetails instanceof User user) {
            extraClaims.put("role", user.getRole().name());
            extraClaims.put("userId", user.getId());
            extraClaims.put("firstName", user.getFirstName());
        }

        return createToken(extraClaims, userDetails, jwtExpiration);
    }

    private String createToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration) {

        return Jwts
                .builder()
                .setClaims(extraClaims)                             // Custom claims first
                .setSubject(userDetails.getUsername())             // sub = email
                .setIssuedAt(new Date(System.currentTimeMillis())) // iat = now
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // exp = now + N ms
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // Sign with our secret
                .compact();                                         // Build the token string
    }
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }
    public long getRemainingValidity(String token) {
        Date expiration = extractExpiration(token);
        long remaining = expiration.getTime() - System.currentTimeMillis();
        return Math.max(remaining, 0);
    }
    public boolean isTokenStructureValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("JWT token is expired: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("JWT token is unsupported: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }


    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())  // Provide the key for verification
                .build()
                .parseClaimsJws(token)          // Parse + verify
                .getBody();                     // Get the payload
    }

    /**
     * Converts the Base64-encoded secret key string to a cryptographic Key object.
     *
     * WHY decode from Base64?
     * The secret in application.yml is Base64-encoded for safe storage in text files.
     * Keys.hmacShaKeyFor() needs the raw bytes.
     *
     * WHY not just use the string directly?
     * The string representation has different entropy than the raw bytes.
     * We need the raw bytes to get the full 256-bit key length for HMAC-SHA256.
     */
    private Key getSignInKey() {
        byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }

}
