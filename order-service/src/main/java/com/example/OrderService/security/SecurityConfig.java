package com.example.OrderService.security;

import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.security.authentication.BasicGrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.GrpcAuthenticationReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Конфигурация безопасности Spring Security.
 * Настраивает аутентификацию, авторизацию и CORS для приложения.
 * Обеспечивает JWT-аутентификацию и защиту эндпоинтов.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    /**
     * Создает бин JwtAuthenticationFilter для обработки JWT токенов.
     * Фильтр проверяет наличие и валидность JWT токена в заголовках запросов.
     *
     * @return настроенный JwtAuthenticationFilter
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
    }

    /**
     * Настраивает цепочку фильтров безопасности HTTP.
     * Определяет политики CORS, CSRF, сессий и авторизации.
     *
     * @param http объект для настройки безопасности HTTP
     * @return сконфигурированная цепочка фильтров безопасности
     * @throws Exception если конфигурация не удалась
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Публичные эндпоинты
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Защищенные эндпоинты
                        .requestMatchers("/api/order").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        // Все остальные запросы требуют аутентификации
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * Настраивает политику CORS (Cross-Origin Resource Sharing) для кросс-доменных запросов.
     * Определяет разрешенные источники, методы, заголовки и другие параметры CORS.
     *
     * @return источник конфигурации CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Разрешенные домены (для продакшена замените на конкретные домены)
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:3000",  // React dev server
                "http://localhost:8080",  // Spring Boot app
                "http://localhost:3001",  // Другие возможные порты
                "https://yourdomain.com"  // Продакшен домен
        ));

        // Разрешенные HTTP методы
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // Разрешенные заголовки
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        // Заголовки, доступные клиенту
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Disposition"
        ));

        // Разрешить отправку учетных данных (cookies, authentication)
        configuration.setAllowCredentials(true);

        // Время кэширования preflight запросов (1 час)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Настраивает провайдер аутентификации DAO (Data Access Object).
     * Использует UserDetailsService для загрузки данных пользователя и PasswordEncoder для проверки паролей.
     *
     * @return настроенный провайдер аутентификации DAO
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Настраивает кодировщик паролей BCrypt.
     * BCrypt автоматически добавляет salt и обеспечивает безопасное хеширование паролей.
     *
     * @return кодировщик паролей BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Настраивает менеджер аутентификации Spring Security.
     * Отвечает за процесс аутентификации пользователей.
     *
     * @param config конфигурация аутентификации Spring
     * @return менеджер аутентификации
     * @throws Exception если конфигурация не удалась
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Настраивает GrpcAuthenticationReader для gRPC безопасности.
     * Этот бин требуется grpc-spring-boot-starter для работы с аутентификацией в gRPC запросах.
     *
     * @return GrpcAuthenticationReader для базовой аутентификации gRPC
     */
    @Bean
    public GrpcAuthenticationReader grpcAuthenticationReader() {
        return new BasicGrpcAuthenticationReader();
    }
}