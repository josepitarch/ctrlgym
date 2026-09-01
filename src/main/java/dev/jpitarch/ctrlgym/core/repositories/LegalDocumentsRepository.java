package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.LegalDocumentVersion;
import dev.jpitarch.ctrlgym.core.domain.enums.LegalDocumentType;
import dev.jpitarch.ctrlgym.core.entities.MemberTermsAcceptanceEntity;
import dev.jpitarch.ctrlgym.core.mappers.LegalDocumentMapper;
import dev.jpitarch.ctrlgym.core.repositories.jpa.LegalDocumentVersionJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MemberTermsAcceptanceJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class LegalDocumentsRepository {

  private final LegalDocumentVersionJpaRepository legalDocumentVersionJpaRepository;

  private final MemberTermsAcceptanceJpaRepository memberTermsAcceptanceJpaRepository;

  private final LegalDocumentMapper legalDocumentMapper;

  public List<LegalDocumentVersion> findAllById(List<UUID> ids) {
    return legalDocumentVersionJpaRepository.findAllById(ids)
      .stream()
      .map(legalDocumentMapper::map)
      .toList();
  }

  public List<LegalDocumentVersion> findAllActiveByGymId(Integer gymId) {
    return legalDocumentVersionJpaRepository.findAllActiveByGymId(gymId)
      .stream()
      .map(legalDocumentMapper::map)
      .toList();
  }

  public void saveAcceptance(MemberTermsAcceptanceEntity entity) {
    memberTermsAcceptanceJpaRepository.save(entity);
  }

}
