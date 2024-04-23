package com.mokhir.dev.ATM.controller;

import com.mokhir.dev.ATM.aggregate.dto.req_dto.CurrencyTypeReqDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.CurrencyTypeResDto;
import com.mokhir.dev.ATM.service.CurrencyTypeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/currency/type")
public class CurrencyTypeController {
    private final CurrencyTypeService currencyTypeService;

    @PostMapping("/create")
    public ResponseEntity<CurrencyTypeResDto>
    create(@Valid @RequestBody CurrencyTypeReqDto reqDto, HttpServletRequest request) {
        return ResponseEntity.ok().body(currencyTypeService.create(reqDto, request));
    }
}
