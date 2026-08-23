package com.fashionpin.authservice.security;

import com.fashionpin.common.security.SecurityConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(SecurityConstants.AUTHORIZATION_HEADER);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(SecurityConstants.BEARER_PREFIX)) {
            String token = authHeader.substring(SecurityConstants.BEARER_PREFIX.length());
            if (jwtService.validateToken(token)) {
                String userId = jwtService.extractUserId(token);
                List<String> roles = jwtService.extractRoles(token);

                List<SimpleGrantedAuthority> authorities = roles != null
                        ? roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())
                        : List.of(new SimpleGrantedAuthority("ROLE_USER"));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }
}
