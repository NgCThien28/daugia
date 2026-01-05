package com.example.daugia.util.excel;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseExport {
    private final XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private final Map<Enum<?>, CellStyle> enumStyleCache = new HashMap<>();

    public BaseExport() {
        this.workbook = new XSSFWorkbook();
    }

    public void createSheet(String sheetName) {
        this.sheet = workbook.createSheet(sheetName);
    }

    public BaseExport writeHeader(String[] headers, int rowIndex) {
        Row row = sheet.createRow(rowIndex);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(12);
        font.setFontName("Times New Roman");
        font.setColor(IndexedColors.BLACK.getIndex());

        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
        return this;
    }

    public <T> void writeData(List<T> listData, String[] fields, Class<T> clazz, int startRow) {
        if (listData == null || listData.isEmpty()) return;

        Map<String, Field> fieldCache = new HashMap<>();
        for (String f : fields) {
            try {
                Field field = clazz.getDeclaredField(f);
                field.setAccessible(true);
                fieldCache.put(f, field);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("Field not found: " + f, e);
            }
        }

        CellStyle textStyle = createDataStyle();
        CellStyle dateStyle = createDateStyle();
        CellStyle numberStyle = createNumberStyle();

        int rowIndex = startRow;

        for (T item : listData) {
            Row row = sheet.createRow(rowIndex++);
            int col = 0;

            for (String f : fields) {
                Object value;
                try {
                    value = fieldCache.get(f).get(item);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

                Cell cell = row.createCell(col);

                if (value instanceof Number) {
                    cell.setCellValue(((Number) value).doubleValue());
                    cell.setCellStyle(numberStyle);
                } else if (value instanceof Date) {
                    cell.setCellValue((Date) value);
                    cell.setCellStyle(dateStyle);
                } else if (value instanceof Boolean) {
                    cell.setCellValue((Boolean) value);
                    cell.setCellStyle(textStyle);
                } else {
                    cell.setCellValue(value != null ? value.toString() : "");
                    cell.setCellStyle(textStyle);
                }
                col++;
            }
        }
        autoSizeColumns(fields.length);
    }

    // STYLES
    private CellStyle createDataStyle() {
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(12);
        font.setFontName("Times New Roman");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDateStyle() {
        CellStyle style = createDataStyle();
        DataFormat df = workbook.createDataFormat();
        style.setDataFormat(df.getFormat("HH:mm:ss dd-MM-yyyy"));
        return style;
    }

    private CellStyle createNumberStyle() {
        CellStyle style = createDataStyle();
        DataFormat df = workbook.createDataFormat();
        style.setDataFormat(df.getFormat("#,##0"));
        return style;
    }

    private CellStyle getEnumStyle(Enum<?> e, IndexedColors color) {
        return enumStyleCache.computeIfAbsent(e, k -> {
            CellStyle style = workbook.createCellStyle();
            XSSFFont font = workbook.createFont();
            font.setFontName("Times New Roman");
            font.setFontHeight(12);
            font.setColor(IndexedColors.BLACK.getIndex());

            style.setFont(font);
            style.setAlignment(HorizontalAlignment.CENTER);
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
            style.setFillForegroundColor(color.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            return style;
        });
    }

    private void autoSizeColumns(int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    public void export(HttpServletResponse response) throws IOException {
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }
}
