package org.server.jwt;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.server.models.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtService {
  @Value("${jwt.expiration}")
  private long expiration;

  private final SecretKey secret;

  private final JwtParser parser;

  public JwtService(@Value("${jwt.secret}") String secret) {
    this.secret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    parser = Jwts.parser().verifyWith(this.secret).build();
  }

  public String generateToken(User user) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("roles", user.getAuthorities());

    return Jwts.builder()
            .claims(claims)
            .subject(user.getUsername())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiration * 1000))
            .signWith(secret, Jwts.SIG.HS256)
            .compact();
  }

  public boolean validateToken(String token) {
    try {
      parser.parseSignedClaims(token);
      return true;
    } catch (Exception e) {
      log.error("Invalid JWT token", e);
      return false;
    }
  }

  public String getUsernameFromToken(String token) {
    return parser
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
  }
}
