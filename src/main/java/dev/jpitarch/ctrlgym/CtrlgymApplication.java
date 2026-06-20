package dev.jpitarch.ctrlgym;

import com.stripe.model.Invoice;
import dev.jpitarch.ctrlgym.verifactu.service.VerifactuService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.resilience.annotation.EnableResilientMethods;

@SpringBootApplication
@EnableResilientMethods
public class CtrlgymApplication {

	static void main(String[] args) {
		SpringApplication.run(CtrlgymApplication.class, args);
	}
}
