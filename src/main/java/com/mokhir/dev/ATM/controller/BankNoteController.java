package com.mokhir.dev.ATM.controller;

import com.mokhir.dev.ATM.aggregate.dto.req_dto.BankNoteReqDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.BankNoteResDto;
import com.mokhir.dev.ATM.service.BankNoteService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/banknote/type")
@RequiredArgsConstructor
public class BankNoteController {
    private final BankNoteService bankNoteService;

    @PostMapping
    public ResponseEntity<BankNoteResDto> createBankNote(
            @RequestBody BankNoteReqDto bankNoteReqDto, HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok().body(bankNoteService.create(bankNoteReqDto, httpServletRequest));
    }
}
