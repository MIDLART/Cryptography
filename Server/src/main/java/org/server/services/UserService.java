package org.crypto.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.server.models.User;
import org.server.repositories.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public void meow() {
    log.info("meow");
  }

  public boolean createUser(User user) {
    String userName = user.getName();
    if (userRepository.findByName(userName) != null) {
      return false;
    }

    user.setPassword(passwordEncoder.encode(user.getPassword()));

    log.info("Saving new User: {}", userName);
    userRepository.save(user);

    return true;
  }

  public User authenticate(String username, String password) {
    User user = userRepository.findByName(username);
    if (user == null) {
      log.warn("User {} not found", username);
      throw new UsernameNotFoundException("User not found");
    }

    if (!passwordEncoder.matches(password, user.getPassword())) {
      log.warn("Invalid password for user {}", username);
      return null;
    }

    log.info("User {} authenticated successfully", username);
    return user;
  }
}
