package dev.jpitarch.ctrlgym.payments.services;

import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class PriceMigrationTask {

  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  public void execute(String productId) {

  }

}
