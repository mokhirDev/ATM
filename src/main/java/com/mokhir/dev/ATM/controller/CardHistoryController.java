package com.mokhir.dev.ATM.controller;

import com.mokhir.dev.ATM.aggregate.dto.req_dto.CardHistoryReqDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.CardHistoryResDto;
import com.mokhir.dev.ATM.service.CardHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/card/history")
@RequiredArgsConstructor
public class CardHistoryController {
    private final CardHistoryService cardHistoryService;

    @GetMapping
    public ResponseEntity<Page<CardHistoryResDto>> getCardHistory(
            @RequestParam int page, @RequestParam int size,
            @RequestBody CardHistoryReqDto cardHistoryReqDto,
            HttpServletRequest request
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CardHistoryResDto> cardHistory = cardHistoryService.getCardHistory(cardHistoryReqDto, request, pageable);
        return ResponseEntity.ok().body(cardHistory);
    }

    @GetMapping("/cashed")
    public ResponseEntity<Page<CardHistoryResDto>> getCardCashingHistory(
            @RequestParam int page, @RequestParam int size,
            @RequestBody CardHistoryReqDto cardHistoryReqDto,
            HttpServletRequest request
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CardHistoryResDto> cardHistory = cardHistoryService
                .getCardCashedHistory(cardHistoryReqDto, request, pageable);
        return ResponseEntity.ok().body(cardHistory);
    }

    @GetMapping("/transferred")
    public ResponseEntity<Page<CardHistoryResDto>> getCardTransferredHistory(
            @RequestParam int page, @RequestParam int size,
            @RequestBody CardHistoryReqDto cardHistoryReqDto,
            HttpServletRequest request
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CardHistoryResDto> cardHistory = cardHistoryService
                .getCardTransferredHistory(cardHistoryReqDto, request, pageable);
        return ResponseEntity.ok().body(cardHistory);
    }

    @GetMapping("/filled")
    public ResponseEntity<Page<CardHistoryResDto>> getCardFilledHistory(
            @RequestParam int page, @RequestParam int size,
            @RequestBody CardHistoryReqDto cardHistoryReqDto,
            HttpServletRequest request
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CardHistoryResDto> cardHistory = cardHistoryService
                .getCardFilledHistory(cardHistoryReqDto, request, pageable);
        return ResponseEntity.ok().body(cardHistory);
    }
    @GetMapping("/date")
    public ResponseEntity<Page<CardHistoryResDto>> getCardDateHistory(
            @RequestParam int page, @RequestParam int size,
            @RequestBody CardHistoryReqDto cardHistoryReqDto,
            HttpServletRequest request
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CardHistoryResDto> cardHistory = cardHistoryService
                .getCardDateHistory(cardHistoryReqDto, request, pageable);
        return ResponseEntity.ok().body(cardHistory);
    }
}
