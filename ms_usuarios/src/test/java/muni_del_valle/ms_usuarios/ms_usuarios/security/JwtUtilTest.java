package muni_del_valle.ms_usuarios.ms_usuarios.security;

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
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 3_600_000L);
    }

    @Test
    void generateToken_sinRol_generaTokenConSubjectCorrecto() {
        String token = jwtUtil.generateToken("user@test.com");

        assertThat(token).isNotBlank();
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("user@test.com");
    }

    @Test
    void generateToken_conRol_incluyeClaimRole() {
        String token = jwtUtil.generateToken("admin@test.com", "ROLE_ADMIN");

        assertThat(jwtUtil.extractRole(token)).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void generateToken_sinRolNulo_noIncluyeClaimRole() {
        String token = jwtUtil.generateToken("user@test.com", null);

        assertThat(jwtUtil.extractRole(token)).isNull();
    }

    @Test
    void extractUsername_tokenValido_retornaEmailCorrecto() {
        String token = jwtUtil.generateToken("service@test.com");

        assertThat(jwtUtil.extractUsername(token)).isEqualTo("service@test.com");
    }

    @Test
    void extractRole_tokenConRoleAdmin_retornaRoleAdmin() {
        String token = jwtUtil.generateToken("admin@test.com", "ROLE_ADMIN");

        assertThat(jwtUtil.extractRole(token)).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void extractRole_tokenSinRole_retornaNull() {
        String token = jwtUtil.generateToken("user@test.com");

        assertThat(jwtUtil.extractRole(token)).isNull();
    }

    @Test
    void validateToken_tokenValido_retornaTrue() {
        String token = jwtUtil.generateToken("user@test.com", "ROLE_USER");

        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_tokenMalformado_retornaFalse() {
        assertThat(jwtUtil.validateToken("token.invalido.malformado")).isFalse();
    }

    @Test
    void validateToken_tokenExpirado_retornaFalse() {
        Key key = Keys.hmacShaKeyFor(SECRET.getBytes());
        Date now = new Date();
        String expired = Jwts.builder()
                .setSubject("old@test.com")
                .setExpiration(new Date(now.getTime() - 1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        assertThat(jwtUtil.validateToken(expired)).isFalse();
    }

    @Test
    void validateToken_tokenConFirmaDistinta_retornaFalse() {
        Key otherKey = Keys.hmacShaKeyFor("otrasecretaclavediferentequenosirveabsolutamente".getBytes());
        String token = Jwts.builder()
                .setSubject("hacker@test.com")
                .signWith(otherKey, SignatureAlgorithm.HS256)
                .compact();

        assertThat(jwtUtil.validateToken(token)).isFalse();
    }
}
