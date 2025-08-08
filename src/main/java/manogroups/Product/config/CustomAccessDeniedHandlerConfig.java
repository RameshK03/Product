package manogroups.Product.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandlerConfig implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String role = auth != null && auth.getAuthorities() != null
                ? auth.getAuthorities().stream().findFirst().map(Object::toString).orElse("UNKNOWN")
                : "ANONYMOUS";

        String path = request.getRequestURI();
        String message = null;

        if (path.startsWith("/api/product/approveCreate") ||
            path.startsWith("/api/product/approveUpdate") ||
            path.startsWith("/api/product/approveDelete") ||
            path.startsWith("/api/product/rejectCreate") ||
            path.startsWith("/api/product/rejectUpdate") ||
            path.startsWith("/api/product/rejectDelete") ||
            path.startsWith("/api/log/product")) {
            message = "Admin only access";

        } else if (path.startsWith("/api/product/add") ||
                   path.startsWith("/api/product/created") ||
                   path.startsWith("/api/product/update") ||
                   path.startsWith("/api/product/updated") ||
                   path.startsWith("/api/product/delete") ||
                   path.startsWith("/api/product/deleted") ||
                   path.startsWith("/api/product/top")) {
            message = "Admin and Staff access only";

        } 

        response.setStatus(HttpServletResponse.SC_FORBIDDEN); 
        response.setContentType("application/json");
        response.getWriter().write(message);
    }
}
