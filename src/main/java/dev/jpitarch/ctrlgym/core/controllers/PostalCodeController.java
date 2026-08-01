package dev.jpitarch.ctrlgym.core.controllers;

import dev.jpitarch.ctrlgym.core.domain.exceptions.ManyPostalCodesException;
import dev.jpitarch.ctrlgym.core.dto.PostalCode;
import dev.jpitarch.ctrlgym.core.repositories.jpa.PostalCodeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/postal-codes")
public class PostalCodeController {

  private final PostalCodeJpaRepository postalCodeJpaRepository;

  @GetMapping("/{postalCode}")
  public ResponseEntity<PostalCode> getByPostalCode(@PathVariable Integer postalCode) {
    var results = postalCodeJpaRepository.findAllByPostalCode(postalCode);

    if (results.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    if (results.size() > 1) {
      throw new ManyPostalCodesException(postalCode);
    }

    var postalCodeMO = results.getFirst();
    return ResponseEntity.ok(new PostalCode(
      postalCodeMO.getPostalCode(),
      postalCodeMO.getCity(),
      postalCodeMO.getProvince(),
      postalCodeMO.getState()
    ));
  }

}
