package com.mokhir.dev.ATM.controller;

import com.mokhir.dev.ATM.aggregate.dto.req_dto.CardTypeReqDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.CardTypeResDto;
import com.mokhir.dev.ATM.service.CardTypeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/card/type")
public class CardTypeController {
    private final CardTypeService cardTypeService;

    @PostMapping("/create")
    public ResponseEntity<CardTypeResDto>
    create(@Valid @RequestBody CardTypeReqDto cardType, HttpServletRequest request) {
        return ResponseEntity.ok().body(cardTypeService.create(cardType, request));
    }

}
