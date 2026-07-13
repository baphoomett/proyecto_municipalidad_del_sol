package muni_del_valle.bff.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final String SECRET = "changeitchangethischangethisisverysecretkeyforjwthmacsha256algorithm";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
    }

    private String buildToken(String subject, String role) {
        Key key = Keys.hmacShaKeyFor(SECRET.getBytes());
        var builder = Jwts.builder().setSubject(subject).signWith(key, SignatureAlgorithm.HS256);
        if (role != null) builder.claim("role", role);
        return builder.compact();
    }

    @Test
    void extractRole_conRolPresenteEnToken_retornaElRol() {
        String token = buildToken("user@test.com", "ROLE_ADMIN");

        assertThat(jwtUtil.extractRole(token)).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void extractRole_conRolUser_retornaRoleUser() {
        String token = buildToken("user@test.com", "ROLE_USER");

        assertThat(jwtUtil.extractRole(token)).isEqualTo("ROLE_USER");
    }

    @Test
    void extractRole_sinClaimRole_retornaNull() {
        String token = buildToken("user@test.com", null);

        assertThat(jwtUtil.extractRole(token)).isNull();
    }
}
