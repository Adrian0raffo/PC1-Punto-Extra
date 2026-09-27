package com.example.week07_lab.auth.infrastructure;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        boolean hasBearer = StringUtils.hasText(authHeader)
                && StringUtils.startsWithIgnoreCase(authHeader, BEARER_PREFIX);

        if (hasBearer) {
            String rawToken = authHeader.substring(BEARER_PREFIX.length()).trim();
            jwtService.extractValidSubject(rawToken)
                    .ifPresent(email -> setAuthentication(request, email));
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(HttpServletRequest request, String email) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(email, null, List.of());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(authToken);
        SecurityContextHolder.setContext(ctx);
    }
}
