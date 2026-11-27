package com.example.helpdesk;

import com.example.helpdesk.security.JwtConfig;
import com.example.helpdesk.security.JwtProperties;
import com.example.helpdesk.security.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtException;

import javax.crypto.SecretKey;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private JwtDecoder decoder;

    @BeforeEach
    void setup() {

        JwtProperties properties = new JwtProperties(
                "test-only-secret-key-for-jwt-validation-2026",
                "helpdesk-statuses",
                Duration.ofMinutes(15)
        );

        JwtConfig config = new JwtConfig();

        SecretKey key = config.jwtSecretKey(properties);

        JwtEncoder encoder = config.jwtEncoder(key);

        decoder = config.jwtDecoder(key, properties);

        jwtService = new JwtService(encoder, properties);
    }

    @Test
    void juniorTokenContainsCorrectClaims() {

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        "junior",
                        null,
                        List.of(
                                new SimpleGrantedAuthority("ROLE_JUNIOR")
                        )
                );

        String token = jwtService.generateToken(authentication);

        Jwt decoded = decoder.decode(token);

        assertEquals("junior", decoded.getSubject());

        assertEquals(
                "helpdesk-statuses",
                decoded.getClaimAsString("iss")
        );

        assertEquals(
                List.of("JUNIOR"),
                decoded.getClaimAsStringList("roles")
        );
    }

    @Test
    void seniorTokenContainsCorrectRole() {

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        "senior",
                        null,
                        List.of(
                                new SimpleGrantedAuthority("ROLE_SENIOR")
                        )
                );

        String token = jwtService.generateToken(authentication);

        Jwt decoded = decoder.decode(token);

        assertEquals("senior", decoded.getSubject());

        assertEquals(
                List.of("SENIOR"),
                decoded.getClaimAsStringList("roles")
        );
    }

    @Test
    void tokenExpiresAfter900Seconds() {

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        "junior",
                        null,
                        List.of(
                                new SimpleGrantedAuthority("ROLE_JUNIOR")
                        )
                );

        Jwt decoded = decoder.decode(
                jwtService.generateToken(authentication)
        );

        long seconds = Duration.between(
                decoded.getIssuedAt(),
                decoded.getExpiresAt()
        ).toSeconds();

        assertEquals(900, seconds);

        assertEquals(900, jwtService.getExpiresIn());
    }

    @Test
    void invalidTokenIsRejected() {

        assertThrows(
                JwtException.class,
                () -> decoder.decode("invalid.jwt.token")
        );
    }

    @Test
    void tokenWithWrongSignatureIsRejected() {

        JwtProperties otherProperties = new JwtProperties(
                "another-test-secret-key-for-jwt-signing-2026",
                "helpdesk-statuses",
                Duration.ofMinutes(15)
        );

        JwtConfig config = new JwtConfig();

        JwtService otherService = new JwtService(
                config.jwtEncoder(
                        config.jwtSecretKey(otherProperties)
                ),
                otherProperties
        );

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        "senior",
                        null,
                        List.of(
                                new SimpleGrantedAuthority("ROLE_SENIOR")
                        )
                );

        String token = otherService.generateToken(authentication);

        assertThrows(
                JwtException.class,
                () -> decoder.decode(token)
        );
    }
}