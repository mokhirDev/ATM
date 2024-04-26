package com.mokhir.dev.ATM.controller;

import com.mokhir.dev.ATM.aggregate.dto.req_dto.CashingTypeReqDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.CashingTypeResDto;
import com.mokhir.dev.ATM.service.CashingTypeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cashingtype")
public class CashingTypeController {
    private final CashingTypeService cashingTypeService;
    @PostMapping
    public ResponseEntity<CashingTypeResDto> create(
            @RequestBody CashingTypeReqDto cashingTypeReqDto,
            HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.ok().body(cashingTypeService.create(cashingTypeReqDto, httpServletRequest));
    }
}
