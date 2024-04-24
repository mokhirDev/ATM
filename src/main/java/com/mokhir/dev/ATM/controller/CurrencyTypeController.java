package com.mokhir.dev.ATM.controller;

import com.mokhir.dev.ATM.aggregate.dto.req_dto.CurrencyTypeReqDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.CurrencyTypeResDto;
import com.mokhir.dev.ATM.service.CurrencyTypeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/currency/type")
public class CurrencyTypeController {
    private final CurrencyTypeService currencyTypeService;

    @PostMapping
    public ResponseEntity<CurrencyTypeResDto>
    create(@Valid @RequestBody CurrencyTypeReqDto reqDto, HttpServletRequest request) {
        return ResponseEntity.ok().body(currencyTypeService.create(reqDto, request));
    }

    @GetMapping
    public ResponseEntity<Page<CurrencyTypeResDto>>
    getAll(@RequestParam("page") int pageIndex, @RequestParam("size") int pageSize, HttpServletRequest request) {
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        return ResponseEntity.ok().body(currencyTypeService.findAll(pageable, request));
    }

    @DeleteMapping
    public ResponseEntity<CurrencyTypeResDto>
    deleteById(@RequestParam("id") Long id, HttpServletRequest request) {
        return ResponseEntity.ok().body(currencyTypeService.deleteById(id, request));
    }

    @PutMapping
    public ResponseEntity<CurrencyTypeResDto>
    updateById(@Valid @RequestBody CurrencyTypeReqDto reqDto, HttpServletRequest request) {
        return ResponseEntity.ok().body(currencyTypeService.updateById(reqDto, request));
    }

}
