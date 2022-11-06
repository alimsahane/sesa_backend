package com.sesa.medical.security.services;

import com.sesa.medical.security.jwt.JwtUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
//@Slf4j
//public class AuthTokenFilter extends OncePerRequestFilter {
public class AuthTokenFilter  {
   /* @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private final String REFRESH_PATH = "/api/auth/refresh";
    private final String EXCEPTION = "exception";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        try {
            String token = jwtUtils.parseJwt(request);
            if (token != null && !uri.equals(REFRESH_PATH)) {
                if (jwtUtils.validateJwtToken(token, jwtUtils.getSecretBearerToken())) {
                    String username = jwtUtils.getUserNameFromJwtToken(token, jwtUtils.getSecretBearerToken());
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null,
                            userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
            if (token != null && uri.equals(REFRESH_PATH)) {
                if (jwtUtils.validateJwtToken(token, jwtUtils.getSecretRefreshToken())) {
                    log.info("valid refresh token");
                }
            }
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
            request.setAttribute(EXCEPTION, e);
        }catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            request.setAttribute(EXCEPTION, e);
        }catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            request.setAttribute(EXCEPTION, e);
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
            request.setAttribute(EXCEPTION, e);
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            request.setAttribute(EXCEPTION, e);
        }catch (Exception e) {
            log.error(e.getMessage());
        }
        filterChain.doFilter(request, response);
    }*/

}
