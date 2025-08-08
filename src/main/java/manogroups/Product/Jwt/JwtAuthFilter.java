package manogroups.Product.Jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    

    @Override
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain) throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        filterChain.doFilter(request, response);
        return;
    }

    String token = authHeader.substring(7);

    try {
        String email = jwtUtil.extractEmail(token);
        String role = jwtUtil.extractRole(token);

        if (email == null || role == null) {
            throw new IllegalArgumentException("Token parsing failed (email or role is null)");
        }

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.toUpperCase());

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                email, null, Collections.singleton(authority));
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authToken);
        filterChain.doFilter(request, response);

    } catch (ExpiredJwtException e) {
        sendErrorResponse(response, "Token has expired", HttpServletResponse.SC_UNAUTHORIZED);
    } catch (SignatureException e) {
        sendErrorResponse(response, "Invalid token signature", HttpServletResponse.SC_UNAUTHORIZED);
    } catch (MalformedJwtException e) {
        sendErrorResponse(response, "Malformed token", HttpServletResponse.SC_UNAUTHORIZED);
    } catch (UnsupportedJwtException e) {
        sendErrorResponse(response, "Unsupported token", HttpServletResponse.SC_UNAUTHORIZED);
    } catch (IllegalArgumentException e) {
        sendErrorResponse(response, "Invalid token: " + e.getMessage(), HttpServletResponse.SC_UNAUTHORIZED);
    } catch (Exception e) {
        // catch any other uncaught errors like decoding failures
        sendErrorResponse(response, "Token processing error: " + e.getMessage(), HttpServletResponse.SC_UNAUTHORIZED);
    }
}


    private void sendErrorResponse(HttpServletResponse response, String message, int status) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(message);
    }
}
