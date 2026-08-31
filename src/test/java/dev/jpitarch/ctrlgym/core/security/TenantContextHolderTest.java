package dev.jpitarch.ctrlgym.core.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TenantContextHolderTest {

  @AfterEach
  void tearDown() {
    TenantContextHolder.clear();
  }

  @Test
  @DisplayName("Returns null when no tenant is set")
  void returnsNullWhenNoTenantSet() {
    assertNull(TenantContextHolder.getTenantId());
  }

  @Test
  @DisplayName("Returns the tenant id after setting it")
  void returnsTenantIdAfterSetting() {
    TenantContextHolder.setTenantId(5);
    assertEquals(5, TenantContextHolder.getTenantId());
  }

  @Test
  @DisplayName("Clears the tenant id")
  void clearsTenantId() {
    TenantContextHolder.setTenantId(5);
    TenantContextHolder.clear();
    assertNull(TenantContextHolder.getTenantId());
  }
}
