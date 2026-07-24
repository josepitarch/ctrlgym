package dev.jpitarch.ctrlgym;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableResilientMethods
@EnableScheduling
public class CtrlgymApplication {

  static void main(String[] args) {
    SpringApplication.run(CtrlgymApplication.class, args);
  }
}
