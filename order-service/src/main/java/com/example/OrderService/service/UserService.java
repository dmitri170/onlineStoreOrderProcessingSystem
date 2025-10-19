package com.example.OrderService.service;

import com.example.OrderService.dto.LoginRequest;
import com.example.OrderService.dto.RegisterRequest;
import com.example.OrderService.entity.Role;
import com.example.OrderService.entity.User;
import com.example.OrderService.repository.UserRepository;
import com.example.OrderService.security.JwtTokenProvider;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        log.debug("Fetching all users");
        return userRepository.findAll();
    }

    /**
     * Находит пользователя по имени пользователя.
     *
     * @param username имя пользователя
     * @return Optional с пользователем, если найден
     */
    public Optional<User> findByUsername(String username) {
        log.debug("Finding user by username: {}", username);
        return userRepository.findByUsername(username);
    }

    /**
     * Находит пользователя по email.
     *
     * @param email email пользователя
     * @return Optional с пользователем, если найден
     */
    public Optional<User> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    /**
     * Находит пользователя по идентификатору.
     *
     * @param id идентификатор пользователя
     * @return Optional с пользователем, если найден
     */
    public Optional<User> findById(Long id) {
        log.debug("Finding user by ID: {}", id);
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
        try {
            log.info("Starting registration for user: {}", registerRequest.getUsername());

            // ИСПРАВЛЕНИЕ: Используйте вспомогательные методы
            if (existsByUsername(registerRequest.getUsername())) {
                log.warn("Registration failed: username already exists - {}", registerRequest.getUsername());
                return ResponseEntity.badRequest().body(Map.of("error", "Имя пользователя уже занято"));
            }

            if (existsByEmail(registerRequest.getEmail())) {
                log.warn("Registration failed: email already exists - {}", registerRequest.getEmail());
                return ResponseEntity.badRequest().body(Map.of("error", "Email уже зарегистрирован"));
            }

            if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
                log.warn("Registration failed: password mismatch for user - {}", registerRequest.getUsername());
                return ResponseEntity.badRequest().body(Map.of("error", "Пароли не совпадают"));
            }

            // ИСПРАВЛЕНИЕ: Используйте метод createUser
            User user = createUser(registerRequest);
            User savedUser = userRepository.save(user);

            log.info("User registered successfully: {} with role: {}", savedUser.getUsername(), user.getRole());

            return ResponseEntity.ok(Map.of(
                    "message", "Пользователь успешно зарегистрирован",
                    "username", user.getUsername(),
                    "role", user.getRole().name(),
                    "email", user.getEmail()
            ));

        } catch (Exception e) {
            log.error("Registration error for user {}: {}", registerRequest.getUsername(), e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Ошибка регистрации: " + e.getMessage()));
        }
    }

    /**
     * Аутентифицирует пользователя и возвращает JWT токен.
     *
     * @param request данные для входа
     * @return ResponseEntity с JWT токеном или ошибкой
     */
    @Transactional(readOnly = true)
    public ResponseEntity<?> login(LoginRequest request) {
        try {
            log.info("Login attempt for user: {}", request.getUsername());

            Optional<User> userOptional = findByUsername(request.getUsername());
            if (userOptional.isEmpty()) {
                log.warn("Login failed: user not found - {}", request.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Неверные учетные данные"));
            }

            User user = userOptional.get();
            log.debug("User found: {}, role: {}", user.getUsername(), user.getRole());

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                log.warn("Login failed: invalid password for user - {}", request.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Неверные учетные данные"));
            }

            String token = jwtTokenProvider.createToken(
                    user.getUsername(),
                    List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
            );

            log.info("User logged in successfully: {}", request.getUsername());

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "role", user.getRole().name(),
                    "username", user.getUsername(),
                    "message", "Вход выполнен успешно"
            ));

        } catch (Exception e) {
            log.error("Login error for user {}: {}", request.getUsername(), e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Ошибка входа: " + e.getMessage()));
        }
    }

    /**
     * Обновляет JWT токен.
     *
     * @param oldToken старый токен
     * @return ResponseEntity с новым токеном или ошибкой
     */
    public ResponseEntity<?> refreshToken(String oldToken) {
        try {
            log.debug("Refreshing token");
            String newToken = jwtTokenProvider.refreshToken(oldToken);
            log.info("Token refreshed successfully");
            return ResponseEntity.ok(Map.of(
                    "token", newToken,
                    "message", "Токен успешно обновлен"
            ));
        } catch (JwtException e) {
            log.warn("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Невалидный или просроченный токен"));
        } catch (Exception e) {
            log.error("Token refresh error: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Ошибка обновления токена: " + e.getMessage()));
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
        // ИСПРАВЛЕНИЕ: Используйте метод getUserById
        User user = getUserById(id);

        // Проверяем, не занят ли email другим пользователем
        if (userDetails.getEmail() != null && !userDetails.getEmail().equals(user.getEmail())) {
            userRepository.findByEmail(userDetails.getEmail())
                    .ifPresent(existingUser -> {
                        if (!existingUser.getId().equals(id)) {
                            log.warn("Update failed: email already taken - {}", userDetails.getEmail());
                            throw new RuntimeException("Email уже занят");
                        }
                    });
            user.setEmail(userDetails.getEmail());
        }

        if (userDetails.getRole() != null) {
            user.setRole(userDetails.getRole());
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", updatedUser.getUsername());
        return updatedUser;
    }

    /**
     * Удаляет пользователя по идентификатору.
     *
     * @param id идентификатор пользователя
     * @throws RuntimeException если пользователь не найден
     */
    @Transactional
    public void deleteUser(Long id) {
        // ИСПРАВЛЕНИЕ: Используйте метод getUserById
        User user = getUserById(id);
        userRepository.delete(user);
        log.info("User deleted successfully: {}", user.getUsername());
    }

    /**
     * Проверяет существование пользователя по имени пользователя.
     *
     * @param username имя пользователя
     * @return true если пользователь существует
     */
    public boolean existsByUsername(String username) {
        log.debug("Checking if username exists: {}", username);
        return userRepository.findByUsername(username).isPresent();
    }

    /**
     * Проверяет существование пользователя по email.
     *
     * @param email email пользователя
     * @return true если пользователь существует
     */
    public boolean existsByEmail(String email) {
        log.debug("Checking if email exists: {}", email);
        return userRepository.findByEmail(email).isPresent();
    }

    /**
     * Создает нового пользователя из запроса регистрации.
     *
     * @param registerRequest данные для регистрации
     * @return созданный пользователь
     */
    public User createUser(RegisterRequest registerRequest) {
        Role role = registerRequest.getRole() != null ? registerRequest.getRole() : Role.USER;

        log.debug("Creating user with username: {} and role: {}", registerRequest.getUsername(), role);
        return new User(
                registerRequest.getUsername(),
                passwordEncoder.encode(registerRequest.getPassword()),
                registerRequest.getEmail(),
                role
        );
    }

    /**
     * Находит пользователя по идентификатору с обработкой исключений.
     *
     * @param id идентификатор пользователя
     * @return пользователь
     * @throws RuntimeException если пользователь не найден
     */
    public User getUserById(Long id) {
        log.debug("Getting user by ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", id);
                    return new RuntimeException("Пользователь не найден с id: " + id);
                });
    }
}