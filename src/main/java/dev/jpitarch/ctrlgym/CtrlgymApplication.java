package dev.jpitarch.ctrlgym;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class CtrlgymApplication {

	static void main(String[] args) {
		SpringApplication.run(CtrlgymApplication.class, args);
	}

	@Bean
  CommandLineRunner commandLineRunner(WebClient webClient) {
    return _ -> {
      var response = webClient
        .get()
        .uri("/health")
        .header("Authorization", "Bearer " + "vf_test_QsjOZPYMRYjc8amKwFXaXL/PI2x8Vm1mvFUtL9DrvDE=")
        .retrieve()
        .bodyToMono(String.class)
        .block();
			System.out.println(response);
    };
  }
}
