package dev.jpitarch.ctrlgym.core.controllers;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@Import(TestSecurityConfig.class)
public abstract class BaseIntegrationTest {

  /*
  TODO
  parece ser que esto hace que la instancia se comparata entre la misma clase de tests
  pero por cada una de ellas se vuelve a levantar una
   */

  @Container
  @ServiceConnection
  static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17.6")
    .withInitScripts("sql/schema.sql", "sql/data.sql");

  @Autowired
  protected MockMvc mockMvc;

}
