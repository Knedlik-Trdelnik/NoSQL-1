package alfarius.yushinon.nosql1.database.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JWTServiceImpl implements JWTService{

    private final Environment environment;

    @Override
    public String generateJWT(String subject) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)))
                .signWith(getSigningKey())
                .compact();
    }

    private Key getSigningKey() {
        return Optional.ofNullable(environment.getProperty("token.secret"))
                .map(String::getBytes)
                .map(Keys::hmacShaKeyFor)
                .orElseThrow(() -> new IllegalArgumentException("Ало бля где токен, животное на разработчике"));
    }
}
