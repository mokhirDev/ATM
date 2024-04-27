package com.mokhir.dev.ATM.controller;

import com.mokhir.dev.ATM.aggregate.dto.req_dto.CashingReqDto;
import com.mokhir.dev.ATM.aggregate.dto.req_dto.FillingReqDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.CashingResDto;
import com.mokhir.dev.ATM.service.CardTransactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/money")
@RequiredArgsConstructor
public class CardTransactionController {
    private final CardTransactionService cashingService;

    @GetMapping("/cash")
    public ResponseEntity<CashingResDto> cash(@Valid @RequestBody CashingReqDto cashingReqDto,
                               HttpServletRequest servletRequest) {
        return ResponseEntity.ok().body(cashingService.cash(cashingReqDto, servletRequest));
    }

    @PostMapping("/fill")
    public ResponseEntity<CashingResDto> fill(@Valid @RequestBody FillingReqDto fillingBalance,
                                              HttpServletRequest servletRequest) {
        return ResponseEntity.ok().body(cashingService.fill(fillingBalance, servletRequest));
    }
}
