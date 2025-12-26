package com.example.daugia.controller;

import com.example.daugia.core.custom.TokenValidator;
import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.request.BaoCaoCreationRequest;
import com.example.daugia.dto.response.PaymentDTO;
import com.example.daugia.entity.Baocao;
import com.example.daugia.service.ActiveTokenService;
import com.example.daugia.service.BaocaoService;
import com.example.daugia.service.PhieuthanhtoanService;
import com.example.daugia.util.excel.BaseExport;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("reports")
public class BaocaoController {
    @Autowired
    private BaocaoService baocaoService;
    @Autowired
    private ActiveTokenService activeTokenService;
    @Autowired
    private TokenValidator tokenValidator;
    @Autowired
    private PhieuthanhtoanService phieuthanhtoanService;

    @GetMapping("/find-all")
    public ApiResponse<List<Baocao>> findAll() {
        List<Baocao> list = baocaoService.findAll();
        return ApiResponse.success(list, "Thành công");
    }

    @PostMapping("/create")
    public ApiResponse<Baocao> create(@RequestBody BaoCaoCreationRequest request,
                                      @RequestHeader("Authorization") String header) {
        String email = tokenValidator.authenticateAndGetEmail(header);
        Baocao saved = baocaoService.create(request, email);
        return ApiResponse.success(saved, "Thành công");
    }

    @PutMapping("/update/{mabc}")
    public ApiResponse<Baocao> update(@PathVariable String mabc,
                                      @RequestBody BaoCaoCreationRequest request,
                                      @RequestHeader("Authorization") String header) {
        String email = tokenValidator.authenticateAndGetEmail(header);
        Baocao updated = baocaoService.update(mabc, request, email);
        return ApiResponse.success(updated, "Cập nhật thành công");
    }

    @DeleteMapping("/delete/{mabc}")
    public ApiResponse<Void> delete(@PathVariable String mabc) {
        baocaoService.delete(mabc);
        return ApiResponse.success(null, "Xoá thành công");
    }

    @GetMapping("/export-excel")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        //setup
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=users_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        //Get list data
        List<PaymentDTO> listPTT = phieuthanhtoanService.findAll();

        new BaseExport<>(listPTT)
                .writeHeaderLine(new String[]{"Mã phiếu", "Ngày thanh toán", "Số tiền"})
                .writeDataLine(new String[]{"matt", "thoigianthanhtoan", "sotien"}, PaymentDTO.class)
                .export(response);
    }
}
