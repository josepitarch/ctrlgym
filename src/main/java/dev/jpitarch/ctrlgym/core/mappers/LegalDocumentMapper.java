package dev.jpitarch.ctrlgym.core.mappers;

import dev.jpitarch.ctrlgym.core.domain.LegalDocumentVersion;
import dev.jpitarch.ctrlgym.core.entities.LegalDocumentVersionEntity;
import org.mapstruct.Mapper;

@Mapper(config = BaseMapper.class)
public interface LegalDocumentMapper {

  LegalDocumentVersion map(LegalDocumentVersionEntity entity);

}
