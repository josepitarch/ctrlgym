package dev.jpitarch.ctrlgym.lib;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RequestHelper {

  public static String extractIp(HttpServletRequest req) {
    String forwardedFor = req.getHeader("X-Forwarded-For");
    if (forwardedFor != null && !forwardedFor.isBlank()) {
      return forwardedFor.split(",")[0].trim();
    }
    return req.getRemoteAddr();
  }

}
