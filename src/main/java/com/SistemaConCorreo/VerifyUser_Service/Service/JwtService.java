package com.SistemaConCorreo.VerifyUser_Service.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    private static final String SECRET_KEY
            = "90f3f5955e2a39143603a240edf662c71de1b17d830af81ec09bd0c873003583";

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    private static final long EXPIRATION_MS = 1000L * 60 * 60;

    public String GenerateUserToken(String username, int idUsuario, String rol) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("idUsuario", idUsuario);
        claims.put("rol", rol);

        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + EXPIRATION_MS))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getExpiration() != null && claims.getExpiration().after(new Date());
        } catch (Exception ex) {
            return false;
        }
    }

    public Claims GetAllClaims(String token) {

        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException ex) {
            return ex.getClaims();
        }
    }

    public String GetUsernameFromToken(String token) {
        return GetAllClaims(token).getSubject();
    }

    public int GetIdUsuarioFromToken(String token) {

        Object value = GetAllClaims(token).get("idUsuario");

        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    public String GetRolFromToken(String token) {
        return (String) GetAllClaims(token).get("rol");
    }

    public boolean IsExpired(String token) {
        try {
            Claims claims = GetAllClaims(token);
            return claims.getExpiration() == null || claims.getExpiration().before(new Date());
        } catch (Exception ex) {
            return true;
        }
    }

    public String GetJtiFromToken(String token) {
        return GetAllClaims(token).getId();
    }

}
