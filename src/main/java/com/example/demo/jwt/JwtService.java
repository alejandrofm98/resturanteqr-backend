package com.example.demo.jwt;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class JwtService {

  @Value("${jwt.secret_key}")
  private String jwtSecretKey;


  public String getToken(UserDetails user) {
    return getToken(new HashMap<>(), user);
  }

  private String getToken(Map<String, Object> extraClaims, UserDetails user) {
    return Jwts
        .builder()
        .setClaims(extraClaims)
        .setSubject(user.getUsername())
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
        .signWith(getKey(), SignatureAlgorithm.HS256).compact();

  }

  private Key getKey() {
    byte[] keyBytes = Decoders.BASE64.decode(jwtSecretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public String getUsernameFromToken(String token) {
    return getClaim(token, Claims::getSubject);
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = getUsernameFromToken(token);
    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
  }

  private Claims getAllClaims(String token) {
    return Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token).getBody();
  }

  public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = getAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Date getExpiration(String token) {
    return getClaim(token, Claims::getExpiration);
  }

  private boolean isTokenExpired(String token) {
    return getExpiration(token).before(new Date());
  }

  public String getTokenFromRequest(HttpServletRequest request) {
    final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }
    return null;
  }


}
