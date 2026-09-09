package io.spring.infrastructure.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@link JwtService} implementation backed by jjwt.
 *
 * <p>Issues HS512-signed tokens whose subject is the user id and which expire {@code
 * jwt.sessionTime} seconds after creation.
 */
@Component
public class DefaultJwtService implements JwtService {
  private final SecretKey signingKey;
  private final SignatureAlgorithm signatureAlgorithm;
  private int sessionTime;

  /**
   * Creates the service.
   *
   * @param secret shared secret used to derive the HS512 signing key, injected from the {@code
   *     jwt.secret} property
   * @param sessionTime token lifetime in seconds, injected from the {@code jwt.sessionTime}
   *     property
   */
  @Autowired
  public DefaultJwtService(
      @Value("${jwt.secret}") String secret, @Value("${jwt.sessionTime}") int sessionTime) {
    this.sessionTime = sessionTime;
    signatureAlgorithm = SignatureAlgorithm.HS512;
    this.signingKey = new SecretKeySpec(secret.getBytes(), signatureAlgorithm.getJcaName());
  }

  /**
   * Issues a signed JWT for the given user. The token's subject is the user id and its expiration
   * is {@code sessionTime} seconds from now.
   *
   * @param user the user to issue a token for; must not be {@code null}
   * @return the compact serialized JWT
   * @throws NullPointerException if {@code user} is {@code null}
   */
  @Override
  public String toToken(User user) {
    return Jwts.builder()
        .setSubject(user.getId())
        .setExpiration(expireTimeFromNow())
        .signWith(signingKey)
        .compact();
  }

  /**
   * Extracts the subject (user id) from a token after verifying its signature and expiration.
   *
   * <p>This method never throws: any failure while parsing or verifying the token (malformed token,
   * wrong signature, expired token, {@code null} input, ...) is swallowed and reported as an empty
   * result.
   *
   * @param token the compact serialized JWT to verify
   * @return the subject claim, or {@link Optional#empty()} if the token is invalid, expired or has
   *     no subject
   */
  @Override
  public Optional<String> getSubFromToken(String token) {
    try {
      Jws<Claims> claimsJws =
          Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token);
      return Optional.ofNullable(claimsJws.getBody().getSubject());
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  private Date expireTimeFromNow() {
    return new Date(System.currentTimeMillis() + sessionTime * 1000L);
  }
}
