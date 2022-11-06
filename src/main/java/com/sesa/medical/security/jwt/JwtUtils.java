package com.sesa.medical.security.jwt;

import io.jsonwebtoken.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Date;
import java.util.Random;

@Component
@Slf4j
@Getter
@Setter
public class JwtUtils {

    @Value("${jwt.uri}")
    private String uri;

    @Value("${jwt.header}")
    private String header;

    @Value("${jwt.prefix}")
    private String prefix;

    @Value("${jwt.expirationBearerTokenInMs}")
    private int expirationBearerToken;

    @Value("${jwt.expirationEmailVerifTokenInMs}")
    private int expirationEmailVerifToken;

    @Value("${jwt.expirationRefreshTokenInMs}")
    private int expirationRefreshToken;

    @Value("${jwt.secretBearerToken}")
    private String secretBearerToken;

    @Value("${jwt.secretRefreshToken}")
    private String secretRefreshToken;

    public String generateJwtToken(String username, int expiration, String secret) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + expiration))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public String getUserNameFromJwtToken(String token, String secret) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getSubject();
    }


    public boolean validateJwtToken(String token, String secret) throws Exception {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (SignatureException | MalformedJwtException | ExpiredJwtException |UnsupportedJwtException|IllegalArgumentException e) {
            throw e;
        }
    }

    public String generateIdTransaction() {
        return "SESA" +"-"+ LocalDate.now().toString().replace("-","")  +"-"+ RandomStringUtils.random(4, 35, 125, true, true, null, new SecureRandom()) +"-"+ (100 + new Random().nextInt(900)) +"-"+ RandomStringUtils.random(4, 35, 125, true, true, null, new SecureRandom());
    }

    public String parseJwt(HttpServletRequest request) {
        String prefixAndToken = request.getHeader(header);
        if (prefixAndToken != null) {
            String tokenOpt = parseJwt(prefixAndToken);
            return tokenOpt;
        }
        return null;
    }

    public String parseJwt(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(prefix)) {
            return bearerToken.substring(7, bearerToken.length());
        }
        return null;
    }

    public String refreshToken(String token) throws Exception {
        String username = getUserNameFromJwtToken(token, secretRefreshToken);
        if (username.isEmpty()) {
            throw new AuthorizationServiceException("Invalid token claims");
        }
        return generateJwtToken(username, expirationRefreshToken, secretRefreshToken);
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
