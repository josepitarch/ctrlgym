package dev.jpitarch.ctrlgym.core.services;

import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.domain.Membership;
import dev.jpitarch.ctrlgym.core.domain.MembershipCancellationReason;
import dev.jpitarch.ctrlgym.core.models.MembershipCancellationReasonTranslationMO;
import dev.jpitarch.ctrlgym.core.repositories.MembershipsRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MembershipCancellationReasonJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipService {

  private final MembershipsRepository membershipsRepository;

  private final MembershipCancellationReasonJpaRepository cancellationReasonJpaRepository;

  private final AcceptHeaderLocaleResolver localeResolver;

  public List<Membership> getMemberships(Member.Id memberId) {
    log.info("Getting memberships for member {}...", memberId);
    return membershipsRepository.getMemberships(memberId);
  }

  public List<MembershipCancellationReason> getCancellationReasons() {
    var language = LocaleContextHolder.getLocale().getLanguage();
    return membershipsRepository.getCancellationReasons(language);
  }

}
