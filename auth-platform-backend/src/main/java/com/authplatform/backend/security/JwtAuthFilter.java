package com.authplatform.backend.security;

import com.authplatform.backend.entity.User;
import com.authplatform.backend.exception.InvalidJwtTokenException;
import com.authplatform.backend.exception.MissingJWTException;
import com.authplatform.backend.exception.UserNotFoundException;
import com.authplatform.backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Collection;

@Service
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private final UserRepository userRepository;
    private final HandlerExceptionResolver exceptionResolver;
    private final JwtService jwtService;

    public JwtAuthFilter(UserRepository userRepository,
                         HandlerExceptionResolver handlerExceptionResolver,
                         JwtService jwtService) {
        this.userRepository = userRepository;
        this.exceptionResolver = handlerExceptionResolver;
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // 1. Skip if user already authenticated or request for public apis
            if (shouldSkip(request) || SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            // 2. Extract Jwt token from bearer
            final String token = extractToken(request);
            // Protected API → must have JWT
            if (token == null) {
                log.warn("Missing Jwt Token");
                throw new MissingJWTException("Missing Bearer token");
            }

            // 3. Validate Token
            if (!jwtService.isValidAndNotExpiredToken(token)) {
                log.warn("Invalid token signature or expired token");
                throw new InvalidJwtTokenException("Invalid token signature or expired token");
            }

            // 4. Extract email from token
            String email = jwtService.getUsernameFromToken(token);
            if (email == null) {
                log.warn("Invalid token signature");
                throw new InvalidJwtTokenException("Invalid token signature");
            }

            // 5. Fetch user from repository ( Database )
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("No one account found this email"));
            Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

            // 6. Set user is authenticated
            UsernamePasswordAuthenticationToken authenticationToken
                    = new UsernamePasswordAuthenticationToken(user, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        } catch (RuntimeException e) {
            log.error("Jwt filter error: {}", e.getMessage());
            exceptionResolver.resolveException(request, response, null, e);
        }

        // 7. Continue filters
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private boolean shouldSkip(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/api/v1/auth/login") ||
                uri.startsWith("/api/v1/auth/register");
    }
}
