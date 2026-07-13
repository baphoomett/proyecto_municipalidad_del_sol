package muni_del_valle.ms_monitoreo.ms_monitoreo.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final String SECRET = "changeitchangethischangethisisverysecretkeyforjwthmacsha256algorithm";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
    }

    private String buildToken(String subject, boolean expired) {
        Key key = Keys.hmacShaKeyFor(SECRET.getBytes());
        Date now = new Date();
        Date exp = expired
                ? new Date(now.getTime() - 1000)
                : new Date(now.getTime() + 3_600_000);
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    void validateToken_tokenValido_retornaTrue() {
        String token = buildToken("monitor-service", false);

        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_tokenExpirado_retornaFalse() {
        String token = buildToken("monitor-service", true);

        assertThat(jwtUtil.validateToken(token)).isFalse();
    }

    @Test
    void validateToken_tokenMalformado_retornaFalse() {
        assertThat(jwtUtil.validateToken("esto.no.es.un.jwt.valido")).isFalse();
    }

    @Test
    void validateToken_tokenNulo_retornaFalse() {
        assertThat(jwtUtil.validateToken(null)).isFalse();
    }

    @Test
    void extractUsername_tokenValido_retornaSubject() {
        String token = buildToken("user@test.com", false);

        assertThat(jwtUtil.extractUsername(token)).isEqualTo("user@test.com");
    }

    @Test
    void extractUsername_tokenInvalido_retornaNull() {
        assertThat(jwtUtil.extractUsername("token.invalido")).isNull();
    }
}
