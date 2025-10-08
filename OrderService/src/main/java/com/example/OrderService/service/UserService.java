package com.example.OrderService.service;

import com.example.OrderService.dto.LoginRequest;
import com.example.OrderService.dto.RegisterRequest;
import com.example.OrderService.entity.Role;
import com.example.OrderService.entity.User;
import com.example.OrderService.repository.UserRepository;
import com.example.OrderService.security.JwtTokenProvider;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Сервис для управления пользователями и аутентификацией.
 * Обрабатывает регистрацию, вход и обновление токенов пользователей.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Находит всех пользователей в системе.
     *
     * @return список всех пользователей
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * Находит пользователя по имени пользователя.
     *
     * @param username имя пользователя
     * @return Optional с пользователем, если найден
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Находит пользователя по email.
     *
     * @param email email пользователя
     * @return Optional с пользователем, если найден
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Находит пользователя по идентификатору.
     *
     * @param id идентификатор пользователя
     * @return Optional с пользователем, если найден
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Регистрирует нового пользователя в системе.
     *
     * @param registerRequest данные для регистрации
     * @return ResponseEntity с результатом регистрации
     */
    @Transactional
    public ResponseEntity<?> registerUser(RegisterRequest registerRequest) {
        if (findByUsername(registerRequest.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Имя пользователя уже занято"));
        }

        if (findByEmail(registerRequest.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email уже зарегистрирован"));
        }

        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Пароли не совпадают"));
        }

        Role role = registerRequest.getRole() != null ? registerRequest.getRole() : Role.USER;

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(role);

        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Пользователь успешно зарегистрирован с ролью: " + role));
    }

    /**
     * Аутентифицирует пользователя и возвращает JWT токен.
     *
     * @param request данные для входа
     * @return ResponseEntity с JWT токеном или ошибкой
     */
    @Transactional(readOnly = true)
    public ResponseEntity<?> login(LoginRequest request) {
        User user = findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Неверные учетные данные"));
        }

        String token = jwtTokenProvider.createToken(
                user.getUsername(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );

        return ResponseEntity.ok(Map.of(
                "token", token,
                "role", user.getRole().name(),
                "username", user.getUsername()
        ));
    }

    /**
     * Обновляет JWT токен.
     *
     * @param oldToken старый токен
     * @return ResponseEntity с новым токеном или ошибкой
     */
    public ResponseEntity<?> refreshToken(String oldToken) {
        try {
            String newToken = jwtTokenProvider.refreshToken(oldToken);
            return ResponseEntity.ok(Map.of("token", newToken));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Невалидный или просроченный токен"));
        }
    }

    /**
     * Обновляет данные пользователя.
     *
     * @param id идентификатор пользователя
     * @param userDetails новые данные пользователя
     * @return обновленный пользователь
     * @throws RuntimeException если пользователь не найден или email уже занят
     */
    @Transactional
    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с id: " + id));

        // Проверяем, не занят ли email другим пользователем
        if (userDetails.getEmail() != null && !userDetails.getEmail().equals(user.getEmail())) {
            userRepository.findByEmail(userDetails.getEmail())
                    .ifPresent(existingUser -> {
                        if (!existingUser.getId().equals(id)) {
                            throw new RuntimeException("Email уже занят");
                        }
                    });
            user.setEmail(userDetails.getEmail());
        }

        if (userDetails.getRole() != null) {
            user.setRole(userDetails.getRole());
        }

        return userRepository.save(user);
    }

    /**
     * Удаляет пользователя по идентификатору.
     *
     * @param id идентификатор пользователя
     * @throws RuntimeException если пользователь не найден
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с id: " + id));
        userRepository.delete(user);
    }
}