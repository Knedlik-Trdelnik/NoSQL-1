package alfarius.yushinon.nosql1.config;

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
                        .requestMatchers(
                                "/",
                                "/madoka_opening.mp3",
                                "/index.html",
                                "/user.html",
                                "/403.html",
                                "/js/**",
                                "/css/**",
                                "/login",
                                "/register",
                                "/api/auth/**",
                                "/api/services/**",
                                "/api/services/classrooms",
                                "/api/services/bookings/my"
                        ).permitAll()
                        .requestMatchers("/api/logout").authenticated()
                        .requestMatchers("/admin.html").permitAll() // <- -- -- Я В РОТ ЕБАЛ СВИНЕЙ
                        .requestMatchers("/admin.html", "/admin/**", "/api/services/bookings/**")
                        .hasAuthority("ADMIN")
                        .anyRequest()
                        .authenticated()


                )
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/403.html")
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