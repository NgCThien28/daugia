package com.example.daugia.util.excel;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.val;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Component
public class BaseExport<T> {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<T> listData;

    public BaseExport(List<T> listData) {
        this.listData = listData;
        workbook = new XSSFWorkbook();
    }

    public BaseExport<T> writeHeaderLine(String[] headers) {
        sheet = workbook.createSheet("data export");
        Row row = sheet.createRow(0);
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(12);
        font.setFontName("Times New Roman");
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        for (int i = 0; i < headers.length; i++) {
            createCell(row, i, headers[i], style);
        }
        return this;
    }

    private void createCell(Row row, int columnCount, Object value, CellStyle style) {
        sheet.autoSizeColumn(columnCount);
        Cell cell = row.createCell(columnCount);
        if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof Date) {
            cell.setCellValue(value.toString());
        } else if (value instanceof BigDecimal) {
            cell.setCellValue(value.toString());
        }
        else {
            cell.setCellValue((String) value);
        }
        cell.setCellStyle(style);
    }

    public BaseExport<T> writeDataLine(String[] fields, Class<T> clazz) {
        int rowCount = 1;
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        style.setFont(font);
        font.setFontHeight(12);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        for (val data : this.listData) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            for (String fieldName : fields) {
                try {
                    Field field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object value = field.get(data);
                    createCell(row, columnCount, value, style);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                columnCount++;
            }
        }
        return this;
    }

    public void export(HttpServletResponse response) throws IOException {
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }
}
