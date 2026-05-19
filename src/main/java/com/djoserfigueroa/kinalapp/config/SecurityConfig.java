package com.djoserfigueroa.kinalapp.config;

import com.djoserfigueroa.kinalapp.service.UsuarioDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth

                        // Rutas públicas
                        .requestMatchers("/login", "/register", "/css/**", "/js/**", "/img/**", "/webjars/**").permitAll()

                        // ─── VISTAS (View Controllers) ────────────────────────────────

                        // Solo ADMIN puede acceder a la gestión de usuarios
                        .requestMatchers("/usuarios/**").hasRole("ADMIN")

                        // ADMIN puede editar y eliminar en cualquier módulo
                        .requestMatchers(
                                "/clientes/editar/**",
                                "/clientes/eliminar/**",
                                "/productos/editar/**",
                                "/productos/eliminar/**",
                                "/ventas/editar/**",
                                "/ventas/eliminar/**",
                                "/detalle-ventas/editar/**",
                                "/detalle-ventas/eliminar/**"
                        ).hasRole("ADMIN")

                        // Ambos roles pueden ver listas y crear nuevos registros
                        .requestMatchers(
                                "/clientes",
                                "/clientes/nuevo",
                                "/clientes/guardar",
                                "/productos",
                                "/productos/nuevo",
                                "/productos/guardar",
                                "/ventas",
                                "/ventas/nuevo",
                                "/ventas/guardar",
                                "/detalle-ventas",
                                "/detalle-ventas/nuevo",
                                "/detalle-ventas/guardar"
                        ).hasAnyRole("ADMIN", "USER")

                        // ─── API REST ─────────────────────────────────────────────────

                        // Solo ADMIN puede DELETE y PUT en la API
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/**").hasRole("ADMIN")

                        // Solo ADMIN puede gestionar usuarios vía API
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")

                        // Ambos pueden GET y POST en la API
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.POST, "/api/**").hasAnyRole("ADMIN", "USER")

                        // Cualquier otra ruta autenticada
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                );

        return http.build();
    }
}