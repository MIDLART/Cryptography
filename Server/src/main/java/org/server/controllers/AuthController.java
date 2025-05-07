package org.server.controllers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.server.jwt.JwtService;
import org.server.models.User;
import org.server.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Log4j2
@Controller
@RequiredArgsConstructor
public class AuthController {
  private final UserService userService;
  private final JwtService jwtService;

  @PostMapping("/registration")
  public ResponseEntity<RegistrationResponse> registerUser(@RequestBody User user) {
    boolean isCreated = userService.createUser(user);

    log.info("User: {}", user);

    if (isCreated) {
      return ResponseEntity.ok(new RegistrationResponse("Регистрация успешна!"));
    } else {
      return ResponseEntity.badRequest().body(
              new RegistrationResponse("Пользователь с таким именем уже существует"));
    }
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
    try {
      User user = userService.authenticate(request.getUsername(), request.getPassword());

      if (user == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new LoginResponse("Неверный пароль", null));
      }

      String token = jwtService.generateToken(user);

      return ResponseEntity.ok(new LoginResponse("Вход выполнен успешно", token));

    } catch (UsernameNotFoundException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
              .body(new LoginResponse("Пользователь не найден", null));
    }
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  private static class RegistrationRequest {
    private String name;
    private String password;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public class RegistrationResponse {
    private String message;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  private static class LoginRequest {
    private String username;
    private String password;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  private static class LoginResponse {
    private String message;
    private String token;
  }
}
