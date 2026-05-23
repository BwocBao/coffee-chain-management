package com.coffeechain.security;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class TokenStore {
  private final Map<String, SessionUser> sessions = new ConcurrentHashMap<>();

  public String createSession(SessionUser user) {
    String token = UUID.randomUUID().toString();
    sessions.put(token, user);
    return token;
  }

  public SessionUser getSession(String token) {
    if (token == null || token.isBlank()) return null;

    SessionUser user = sessions.get(token);
    if (user == null) return null;

    if (user.getExpiredAt() != null && user.getExpiredAt().isBefore(LocalDateTime.now())) {
      sessions.remove(token);
      return null;
    }

    return user;
  }

  public void removeSession(String token) {
    if (token != null) sessions.remove(token);
  }
}
