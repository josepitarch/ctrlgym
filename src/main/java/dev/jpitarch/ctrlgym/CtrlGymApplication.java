package dev.jpitarch.ctrlgym;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableResilientMethods
public class CtrlGymApplication {

  static void main(String[] args) {
    SpringApplication.run(CtrlGymApplication.class, args);
  }

}
