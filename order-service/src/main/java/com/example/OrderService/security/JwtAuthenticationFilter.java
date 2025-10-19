package com.example.OrderService.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Фильтр для аутентификации JWT токенов.
 * Перехватывает HTTP запросы и проверяет JWT токен в заголовке Authorization.
 * Устанавливает аутентификацию в SecurityContext если токен валиден.
 *
 * Важно: Этот фильтр не должен быть аннотирован @Component чтобы избежать
 * циклических зависимостей. Создается как бин в SecurityConfig.
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    /**
     * Обрабатывает каждый HTTP запрос для JWT аутентификации.
     * Извлекает JWT токен из заголовка Authorization, проверяет его валидность
     * и устанавливает аутентификацию в SecurityContext если токен корректен.
     *
     * @param request HTTP запрос
     * @param response HTTP ответ
     * @param filterChain цепочка фильтров для продолжения обработки
     * @throws ServletException если возникает ошибка сервлета
     * @throws IOException если возникает ошибка ввода-вывода
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                if (jwtTokenProvider.validateToken(jwt)) {
                    String username = jwtTokenProvider.getUsernameFromToken(jwt);

                    // Загружаем данные пользователя только если аутентификация еще не установлена
                    if (SecurityContextHolder.getContext().getAuthentication() == null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        // Создаем объект аутентификации Spring Security
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // Устанавливаем аутентификацию в контекст безопасности
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        log.debug("Successfully authenticated user: {}", username);
                    }
                }
            }
        } catch (Exception e) {
            // Логируем ошибку, но продолжаем выполнение цепочки фильтров
            // Пользователь останется неаутентифицированным
            log.warn("Failed to process JWT authentication: {}", e.getMessage());
        }

        // Продолжаем выполнение цепочки фильтров
        filterChain.doFilter(request, response);
    }

    /**
     * Извлекает JWT токен из заголовка Authorization HTTP запроса.
     * Ожидает формат: "Bearer <token>"
     *
     * @param request HTTP запрос для извлечения токена
     * @return JWT токен или null если токен не найден или формат неверный
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // Извлекаем токен без префикса "Bearer "
            return bearerToken.substring(7);
        }

        return null;
    }

    /**
     * Определяет, должен ли фильтр применяться к данному запросу.
     * Можно переопределить для исключения определенных путей.
     *
     * @param request HTTP запрос
     * @return true если фильтр должен быть применен, false в противном случае
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Можно добавить исключения для определенных путей
        String path = request.getServletPath();
        return path.startsWith("/auth/") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs/");
    }
}