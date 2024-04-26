package com.mokhir.dev.ATM.controller;

import com.mokhir.dev.ATM.aggregate.dto.req_dto.CashingReqDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.CashingResDto;
import com.mokhir.dev.ATM.service.CashingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cash")
@RequiredArgsConstructor
public class CashingController {
    private final CashingService cashingService;

    @PostMapping
    public ResponseEntity<CashingResDto> cash(@Valid @RequestBody CashingReqDto cashingReqDto,
                               HttpServletRequest servletRequest) {
        return ResponseEntity.ok().body(cashingService.cash(cashingReqDto, servletRequest));
    }
}
