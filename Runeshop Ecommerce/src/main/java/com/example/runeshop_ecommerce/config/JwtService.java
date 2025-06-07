package com.example.runeshop_ecommerce.config;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private static final String SECRET_KEY="UnVuZXNob3BFY29tbWVyY2VNZWpvckdydXBvSXNsYXNUaWtpVGlraQ==";
    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 15;

    public String getToken(UserDetails userDetails) {
        return getToken(new HashMap<>(), userDetails);
    }


    private String getToken(Map<String, Object> extraClaims, UserDetails user) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+ ACCESS_TOKEN_EXPIRATION))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    public String getUsernameFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    private Claims getAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private boolean isTokenExpired(String token) {
        Date expiration = getClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String nombreUsuario = getUsernameFromToken(token);
        return (nombreUsuario.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
