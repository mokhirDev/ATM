package com.mokhir.dev.ATM.controller;

import com.mokhir.dev.ATM.aggregate.dto.req_dto.CardHolderReqDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.CardHolderResDto;
import com.mokhir.dev.ATM.service.CardHolderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cardholder")
@RequiredArgsConstructor
public class CardHolderController {
    private final CardHolderService cardHolderService;

    @PostMapping("/create")
    public ResponseEntity<CardHolderResDto> createCardHolder(
            @Valid @RequestBody CardHolderReqDto cardHolderReqDto,
            HttpServletRequest request) {
        return ResponseEntity.ok().body(cardHolderService.createCardHolder(cardHolderReqDto, request));
    }
}
