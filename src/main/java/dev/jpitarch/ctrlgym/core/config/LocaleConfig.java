package dev.jpitarch.ctrlgym.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;
import java.util.List;

@Configuration(proxyBeanMethods = false)
public class LocaleConfig {

  @Bean
  AcceptHeaderLocaleResolver localeResolver() {
    var resolver = new AcceptHeaderLocaleResolver();
    resolver.setDefaultLocale(Locale.forLanguageTag("es"));
    resolver.setSupportedLocales(List.of(
      Locale.forLanguageTag("es"),
      Locale.forLanguageTag("ca"),
      Locale.forLanguageTag("en")
    ));

    return resolver;
  }

}
