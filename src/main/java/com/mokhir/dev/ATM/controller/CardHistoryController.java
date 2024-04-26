package com.mokhir.dev.ATM.controller;

import com.mokhir.dev.ATM.aggregate.dto.req_dto.CardHistoryReqDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.CardHistoryResDto;
import com.mokhir.dev.ATM.service.CardHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        List<CardHistoryResDto> cardHistory = cardHistoryService.getCardHistory(cardHistoryReqDto, request);
        return ResponseEntity.ok().body(new PageImpl<>(cardHistory, pageable, cardHistory.size()));
    }
}
