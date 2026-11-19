package alfarius.yushinon.nosql1.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
public class WebSecurityConfig {


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/logout").authenticated()
                        .requestMatchers(
                                "/admin/**",
                                "/api/services/bookings/**",
                                "/api/logs/**"
                        ).hasRole("ADMIN")
                        .requestMatchers(
                                "/", "/index.html", "/user.html", "/cassandra.html", "/403.html",
                                "/admin.html",              // страница открыта, данные защищены API
                                "/madoka_opening.mp3",
                                "/js/**", "/css/**",
                                "/login", "/register",
                                "/api/auth/**",
                                "/api/services/classrooms",
                                "/api/analytics/popular-tags",
                                "/api/cassandra/**"         // + путь вашего write-эндпоинта
                        ).permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) ->
                                res.sendError(HttpServletResponse.SC_UNAUTHORIZED))   // 401 вместо пустого 403
                        .accessDeniedHandler((req, res, e) ->
                                res.sendRedirect("/403.html"))
                )
                .formLogin(form -> form.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                ;

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}