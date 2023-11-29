package com.symbolic.symbolic.configuration.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Custom service class for generating the JSON Web Tokens used for authorization.
 */
@Service
public class JwtService {
  private static final String SECRET_KEY
      = "244226452948404D635166546A576E5A7234753778214125432A462D4A614E64";

  public String extractUserNameOrEmail(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  public String generateToken(UserDetails userDetails) {
    return generateToken(new HashMap<>(), userDetails);
  }

  /**
   * Generates a JSON web token based off of a UserDetails object and any extra fields.
   *
   * @param extraClaims the extra fields to be added to the token
   * @param userDetails the User object for which the token will be generated
   * @return a JSON web token for the User in the form of a String
   */
  public String generateToken(
      Map<String, Object> extraClaims,
      UserDetails userDetails
  ) {
    return Jwts
        .builder()
        .setClaims(extraClaims)
        .setSubject(userDetails.getUsername())
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 60))
        .signWith(getSinInKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  /**
   * Validates whether a JSON web token is still valid or has expired.
   *
   * @param token a JSON web token in the form of a String
   * @param userDetails the User that the JSON web token will be checked against
   * @return a boolean value whether the token matches the User and has not expired
   */
  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUserNameOrEmail(token);
    return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);

  }

  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  // ectract claim
  public <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
    final Claims claims = extractAllClaims(token);
    return claimResolver.apply(claims);
  }

  // extract all claims
  private Claims extractAllClaims(String token) {
    return Jwts
        .parserBuilder()
        .setSigningKey(getSinInKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  private Key getSinInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
