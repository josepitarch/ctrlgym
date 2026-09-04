package dev.jpitarch.ctrlgym.core.controllers;

import dev.jpitarch.ctrlgym.core.dto.PostalCode;
import dev.jpitarch.ctrlgym.core.repositories.jpa.PostalCodeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/postal-codes")
public class PostalCodeController {

  private final PostalCodeJpaRepository postalCodeJpaRepository;

  @GetMapping("/{postalCode}")
  public ResponseEntity<List<PostalCode>> getByPostalCode(@PathVariable Integer postalCode) {
    var results = postalCodeJpaRepository.findAllByPostalCode(postalCode);

    if (results.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    var postalCodes = results.stream()
      .map(entity -> new PostalCode(
        entity.getPostalCode(),
        entity.getCity(),
        entity.getProvince(),
        entity.getState()
      ))
      .toList();

    return ResponseEntity.ok(postalCodes);
  }

}
