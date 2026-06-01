package dev.jpitarch.ctrlgym;

import com.stripe.model.Account;
import com.stripe.service.AccountService;
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

}
