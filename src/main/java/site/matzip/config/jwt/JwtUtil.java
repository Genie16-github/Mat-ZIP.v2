package site.matzip.config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET_KEY = "MySuperSecretKeyForJwt1234567890"; // 32B+
    private static final long EXPIRATION_TIME = 60 * 60 * 1000L; // 1시간

    private final Key key;
    private final JwtParser parser;

    public JwtUtil() {
        this.key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        this.parser = Jwts.parserBuilder()
                .setSigningKey(key)              // ✅ 0.11.x는 setSigningKey
                .setAllowedClockSkewSeconds(60)
                .build();
    }

    /** 생성 */
    public String generateToken(String username) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)  // ✅ 0.11.x 방식
                .compact();
    }

    /** 검증 */
    public boolean validateToken(String token) {
        try {
            parser.parseClaimsJws(token);                 // ✅ 파싱=검증
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /** username(sub) 추출 */
    public String extractUsername(String token) {
        return parser.parseClaimsJws(token).getBody().getSubject();
    }
}
