package dev.jpitarch.ctrlgym.core.services;

import dev.jpitarch.ctrlgym.core.domain.DatePeriod;
import dev.jpitarch.ctrlgym.core.domain.Expense;
import dev.jpitarch.ctrlgym.core.domain.GymBranchId;
import dev.jpitarch.ctrlgym.core.models.ExpenseCategoryMO;
import dev.jpitarch.ctrlgym.core.repositories.ExpensesRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExpensesService {

  private final ExpensesRepository expensesRepository;

  public List<ExpenseCategoryMO> getAllCategories() {
    return expensesRepository.getAllCategories();
  }

  public List<Expense> getExpenses(GymBranchId gymBranchId) {
    return expensesRepository.getExpenses(gymBranchId);
  }

  public Map<YearMonth, Double> getTotalPerMonth(GymBranchId gymBranchId, DatePeriod datePeriod) {
    return expensesRepository.getTotalPerMonth(gymBranchId, datePeriod);
  }

  public byte[] generateExpensesExcel() throws IOException {
    List<ExpenseCategoryMO> categories = getAllCategories();

    try (Workbook workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet("Gastos");

      // Crear estilo para el título
      CellStyle titleStyle = workbook.createCellStyle();
      Font titleFont = workbook.createFont();
      titleFont.setBold(true);
      titleFont.setFontHeightInPoints((short) 18);
      titleFont.setColor(IndexedColors.WHITE.getIndex());
      titleStyle.setFont(titleFont);
      titleStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
      titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
      titleStyle.setAlignment(HorizontalAlignment.CENTER);

      // Crear fila del título
      Row titleRow = sheet.createRow(0);
      Cell titleCell = titleRow.createCell(0);
      titleCell.setCellValue("HOJA DE GASTOS");
      titleCell.setCellStyle(titleStyle);

      // Fusionar celdas para el título
      sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 2));

      // Crear estilo para los encabezados de columna
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

      // Crear fila de encabezados
      Row headerRow = sheet.createRow(2);
      Cell headerCell = headerRow.createCell(0);
      headerCell.setCellValue("Código");
      headerCell.setCellStyle(headerStyle);

      // Crear estilo para las celdas de datos
      CellStyle dataStyle = workbook.createCellStyle();
      dataStyle.setBorderBottom(BorderStyle.THIN);
      dataStyle.setBorderTop(BorderStyle.THIN);
      dataStyle.setBorderLeft(BorderStyle.THIN);
      dataStyle.setBorderRight(BorderStyle.THIN);

      // Llenar datos
      int rowNum = 3;
      for (ExpenseCategoryMO category : categories) {
        Row row = sheet.createRow(rowNum++);
        Cell cell = row.createCell(0);
        cell.setCellValue(category.getCode());
        cell.setCellStyle(dataStyle);
      }

      // Ajustar ancho de columna
      sheet.autoSizeColumn(0);

      // Escribir a byte array
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      workbook.write(outputStream);
      return outputStream.toByteArray();
    }
  }

}
