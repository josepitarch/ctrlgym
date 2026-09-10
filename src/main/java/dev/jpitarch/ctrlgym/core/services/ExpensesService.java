package dev.jpitarch.ctrlgym.core.services;

import dev.jpitarch.ctrlgym.core.domain.DateRange;
import dev.jpitarch.ctrlgym.core.domain.Expense;
import dev.jpitarch.ctrlgym.core.domain.ExpenseCategory;
import dev.jpitarch.ctrlgym.core.domain.GymBranchId;
import dev.jpitarch.ctrlgym.core.entities.ExpenseCategoryEntity;
import dev.jpitarch.ctrlgym.core.repositories.ExpensesRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.ExpenseCategoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExpensesService {

  private final ExpensesRepository expensesRepository;

  private final ExpenseCategoryJpaRepository expenseCategoryJpaRepository;

  public List<ExpenseCategoryEntity> getAllCategories(Integer gymId) {
    return expensesRepository.getAllCategories(gymId);
  }

  public List<ExpenseCategory> getCategories(Integer gymId) {
    return getAllCategories(gymId).stream()
      .map(this::toDomain)
      .toList();
  }

  public List<Expense> getExpenses(GymBranchId gymBranchId) {
    return expensesRepository.getExpenses(gymBranchId);
  }

  public Map<YearMonth, Double> getTotalPerMonth(GymBranchId gymBranchId, DateRange dateRange) {
    return expensesRepository.getTotalPerMonth(gymBranchId, dateRange);
  }

  @Transactional
  public ExpenseCategory createCategory(Integer gymId, ExpenseCategory category) {
    ExpenseCategoryEntity entity = new ExpenseCategoryEntity();
    entity.setGymId(gymId);
    entity.setName(category.getName());
    entity.setIsActive(true);
    ExpenseCategoryEntity saved = expenseCategoryJpaRepository.save(entity);
    return toDomain(saved);
  }

  @Transactional
  public void deleteCategory(Integer categoryId, Integer gymId) {
    ExpenseCategoryEntity entity = expenseCategoryJpaRepository.findById(categoryId)
      .orElseThrow(() -> new IllegalArgumentException("Expense category with id " + categoryId + " not found"));
    if (!entity.getGymId().equals(gymId)) {
      throw new IllegalArgumentException("Expense category with id " + categoryId + " does not belong to gym " + gymId);
    }
    entity.setIsActive(false);
    expenseCategoryJpaRepository.save(entity);
  }

  @Transactional
  public ExpenseCategory updateCategoryName(Integer categoryId, Integer gymId, String newName) {
    ExpenseCategoryEntity entity = expenseCategoryJpaRepository.findById(categoryId)
      .orElseThrow(() -> new IllegalArgumentException("Expense category with id " + categoryId + " not found"));
    if (!entity.getGymId().equals(gymId)) {
      throw new IllegalArgumentException("Expense category with id " + categoryId + " does not belong to gym " + gymId);
    }
    entity.setName(newName);
    ExpenseCategoryEntity saved = expenseCategoryJpaRepository.save(entity);
    return toDomain(saved);
  }

  private ExpenseCategory toDomain(ExpenseCategoryEntity entity) {
    return ExpenseCategory.builder()
      .id(entity.getId())
      .gymId(entity.getGymId())
      .name(entity.getName())
      .build();
  }

  public byte[] generateExpensesExcel(Integer gymId) throws IOException {
    List<ExpenseCategoryEntity> categories = getAllCategories(gymId);

    try (Workbook workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet("Gastos");

      CellStyle titleStyle = workbook.createCellStyle();
      Font titleFont = workbook.createFont();
      titleFont.setBold(true);
      titleFont.setFontHeightInPoints((short) 18);
      titleFont.setColor(IndexedColors.WHITE.getIndex());
      titleStyle.setFont(titleFont);
      titleStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
      titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
      titleStyle.setAlignment(HorizontalAlignment.CENTER);

      Row titleRow = sheet.createRow(0);
      Cell titleCell = titleRow.createCell(0);
      titleCell.setCellValue("HOJA DE GASTOS");
      titleCell.setCellStyle(titleStyle);

      sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 2));

      CellStyle headerStyle = workbook.createCellStyle();
      Font headerFont = workbook.createFont();
      headerFont.setBold(true);
      headerStyle.setFont(headerFont);
      headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
      headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
      headerStyle.setBorderBottom(BorderStyle.THIN);
      headerStyle.setBorderTop(BorderStyle.THIN);
      headerStyle.setBorderLeft(BorderStyle.THIN);
      headerStyle.setBorderRight(BorderStyle.THIN);

      Row headerRow = sheet.createRow(2);
      Cell headerCell = headerRow.createCell(0);
      headerCell.setCellValue("Nombre");
      headerCell.setCellStyle(headerStyle);

      CellStyle dataStyle = workbook.createCellStyle();
      dataStyle.setBorderBottom(BorderStyle.THIN);
      dataStyle.setBorderTop(BorderStyle.THIN);
      dataStyle.setBorderLeft(BorderStyle.THIN);
      dataStyle.setBorderRight(BorderStyle.THIN);

      int rowNum = 3;
      for (ExpenseCategoryEntity category : categories) {
        Row row = sheet.createRow(rowNum++);
        Cell cell = row.createCell(0);
        cell.setCellValue(category.getName());
        cell.setCellStyle(dataStyle);
      }

      sheet.autoSizeColumn(0);

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      workbook.write(outputStream);
      return outputStream.toByteArray();
    }
  }

}
