package dev.jpitarch.ctrlgym.notifications;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class EmailTemplateComponent {

  public String build(String templateName, Map<String, Object> variables) {
    String template = this.read(templateName);
    for (Map.Entry<String, Object> entry : variables.entrySet()) {
      String placeholder = "{{ " + "." + entry.getKey() + " }}";
      template = template.replace(placeholder, entry.getValue().toString());
    }

    return template;
  }

  private String read(String templateName) {
    try {
      return new String(
        new ClassPathResource("templates/emails/" + templateName)
          .getInputStream()
          .readAllBytes());
    } catch (IOException e) {
      throw new RuntimeException("Error loading email template: " + templateName, e);
    }
  }


}
