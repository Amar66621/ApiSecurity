package com.main.security;

import java.io.IOException;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {

        // Fetching the Authorization header
        String requestHeader = request.getHeader("Authorization");
        logger.info("Authorization Header : {}", requestHeader);

        String username = null;
        String token = null;

        // Checking if the header has a Bearer token
        if (requestHeader != null && requestHeader.startsWith("Bearer ")) {
            token = requestHeader.substring(7);
            try {
                // Getting username from token
                username = jwtHelper.getUsernameFromToken(token);
            } catch (IllegalArgumentException e) {
                logger.error("Illegal argument exception while retrieving username from token.", e);
            } catch (ExpiredJwtException e) {
                logger.error("JWT token is expired.", e);
            } catch (MalformedJwtException e) {
                logger.error("Invalid JWT token format.", e);
            } catch (Exception e) {
                logger.error("Unexpected error while parsing token.", e);
            }
        } else {
            logger.warn("Authorization header is either missing or does not start with Bearer.");
        }

        // Validating the token if username is available and there is no existing authentication
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // Validate the token with user details
            if (jwtHelper.validateToken(token, userDetails)) {
                // Creating UsernamePasswordAuthenticationToken and setting it in the context
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.info("Authenticated user: {}", username);
            } else {
                logger.warn("JWT token validation failed.");
            }
        }

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }
}
