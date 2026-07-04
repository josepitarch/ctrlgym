package dev.jpitarch.ctrlgym.core.services;

import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.domain.Membership;
import dev.jpitarch.ctrlgym.core.repositories.MembershipsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipService {

  private final MembershipsRepository membershipsRepository;

  public List<Membership> getMemberships(Member.Id memberId) {
    log.info("Getting memberships for member {}...", memberId);
    return membershipsRepository.getMemberships(memberId);
  }

}
