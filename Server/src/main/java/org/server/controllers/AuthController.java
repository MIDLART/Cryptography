package org.server.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.server.dto.LoginRequest;
import org.server.dto.LoginResponse;
import org.server.dto.RegistrationResponse;
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
    if (!isValid(user.getUsername())) {
      return ResponseEntity.badRequest().body(
              new RegistrationResponse("Имя пользователя невалидно. Имя должно содержать до 25 символов (буквы, цифры, _)"));
    }

    if (!isValid(user.getPassword())) {
      return ResponseEntity.badRequest().body(
              new RegistrationResponse("Пароль невалиден. Он должен содержать до 25 символов (буквы, цифры, _)"));
    }

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

  private boolean isValid(String str) {
    if (str == null || str.isEmpty() || str.length() > 25) {
      return false;
    }
    return str.matches("^[a-zA-Z0-9_]+$");
  }
}
