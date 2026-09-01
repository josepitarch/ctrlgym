package dev.jpitarch.ctrlgym.core.services;

import dev.jpitarch.ctrlgym.core.dto.GuardianAuthorizationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuardianAuthorizationService {

  public GuardianAuthorizationDto getByToken(String token) {
    return new GuardianAuthorizationDto(null, null, null, null, null, true, null);
  }

  public void approve(String token, String ip, String userAgent) {
  }
}
