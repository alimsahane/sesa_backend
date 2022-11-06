package com.sesa.medical.security;


import com.sesa.medical.globalconfig.AppProperties;
import com.sesa.medical.users.entities.Users;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Random;

@Service
public class TokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(TokenProvider.class);
    private static final String AUTHENTICATED = "authenticated";
    public static final long TEMP_TOKEN_VALIDITY_IN_MILLIS = 300000;
    @Value("${jwt.prefix}")
    private String prefix;
    private AppProperties appProperties;

    public TokenProvider(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public String createToken(Authentication authentication, boolean authenticated) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (authenticated ? appProperties.getAuth().getTokenExpirationMsec() : TEMP_TOKEN_VALIDITY_IN_MILLIS));

        return Jwts.builder().setSubject(Long.toString(userPrincipal.getId())).claim(AUTHENTICATED, authenticated).setIssuedAt(new Date()).setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, appProperties.getAuth().getTokenSecret()).compact();

    }

    public String createTokenLocalUser(Users userPrincipal, boolean authenticated) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (authenticated ? appProperties.getAuth().getTokenExpirationMsec() : TEMP_TOKEN_VALIDITY_IN_MILLIS));

        return Jwts.builder().setSubject(Long.toString(userPrincipal.getUserId())).claim(AUTHENTICATED, authenticated).setIssuedAt(new Date()).setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, appProperties.getAuth().getTokenSecret()).compact();
    }

    public String createTokenRefresh(Users userPrincipal, boolean authenticated) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (authenticated ? appProperties.getAuth().getRefrestTokenExpirationMsec() : TEMP_TOKEN_VALIDITY_IN_MILLIS));

        return Jwts.builder().setSubject(Long.toString(userPrincipal.getUserId())).claim(AUTHENTICATED, authenticated).setIssuedAt(new Date()).setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, appProperties.getAuth().getTokenSecret()).compact();
    }

    public String createTokenRefresh(Authentication authentication, boolean authenticated) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (authenticated ? appProperties.getAuth().getRefrestTokenExpirationMsec() : TEMP_TOKEN_VALIDITY_IN_MILLIS));

        return Jwts.builder().setSubject(Long.toString(userPrincipal.getId())).claim(AUTHENTICATED, authenticated).setIssuedAt(new Date()).setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, appProperties.getAuth().getTokenSecret()).compact();

    }
    public String parseJwt(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(prefix)) {
            return bearerToken.substring(7, bearerToken.length());
        }
        return null;
    }

    public Boolean isAuthenticated(String token) {
        Claims claims = Jwts.parser().setSigningKey(appProperties.getAuth().getTokenSecret()).parseClaimsJws(token).getBody();
        return claims.get(AUTHENTICATED, Boolean.class);
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(appProperties.getAuth().getTokenSecret())
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }

    public int generateOtpCode() {
        return  (1000 + new Random().nextInt(9000));
    }


    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(appProperties.getAuth().getTokenSecret()).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty.");
        }
        return false;
    }

}
