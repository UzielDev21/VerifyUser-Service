package com.SistemaConCorreo.VerifyUser_Service.Filter;

import com.SistemaConCorreo.VerifyUser_Service.Service.JwtService;
import com.SistemaConCorreo.VerifyUser_Service.Service.TokenBlackListService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import org.springframework.security.core.userdetails.UserDetailsService;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenBlackListService tokenBlackListService;

    public JwtAuthFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService,
            TokenBlackListService tokenBlackListService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.tokenBlackListService = tokenBlackListService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getServletPath();
        return path.startsWith("/api/login")
                || path.startsWith("/api/usuario")
                || path.startsWith("/api/auth/verify")
                || path.startsWith("/auth/login")
                || path.startsWith("/css/**")
                || path.startsWith("/js/**")
                || path.startsWith("/images/**")
                || path.equals("/error");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtService.isTokenValid(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token invalido");
            return;
        }

        Claims claims = jwtService.GetAllClaims(token);
        String username = claims.getSubject();
        String jti = claims.getId();

        if (tokenBlackListService.isTokenInvalid(jti)) {

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Token inhabilitado por logout");
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (!userDetails.isEnabled()) {
                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Usuario deshabilitado"
                );
                return;
            }

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities());

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

        }
        filterChain.doFilter(request, response);
    }
}
