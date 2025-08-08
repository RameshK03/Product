package manogroups.Product.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import manogroups.Product.Jwt.JwtAuthFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private CustomAccessDeniedHandlerConfig accessDeniedHandler;

    @Autowired
    private JwtAuthFilter jwtAuthFilter;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(csrf -> csrf.disable())
        .formLogin(form -> form.disable())
        .authorizeHttpRequests(auth -> auth
            // .requestMatchers(
            //     "/api/product/approved/{storeName}",
            //     "/api/product/category",
            //     "/api/product/{productId}",
            //     "/api/product/productQuantity/{productId}").permitAll()

            // .requestMatchers(
            //     "/api/product/add",
            //     "/api/product/created",
            //     "/api/product/update",
            //     "/api/product/updated",
            //     "/api/product/delete/{productId}",
            //     "/api/product/deleted",
            //     "/api/product/top/{storeName}").hasAnyRole("ADMIN","STAFF")

            // .requestMatchers(
            //     "/api/product/approveCreate",
            //     "/api/product/approveUpdate",
            //     "/api/product/approveDelete",
            //     "/api/product/rejectCreate",
            //     "/api/product/rejectUpdate",
            //     "/api/product/rejectDelete",
            //     "/api/log/product").hasRole("ADMIN")

            .anyRequest().permitAll()
        )
        .exceptionHandling(ex -> ex.accessDeniedHandler(accessDeniedHandler))
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
    }

}
