package com.mokhir.dev.ATM.controller;

import com.mokhir.dev.ATM.aggregate.dto.req_dto.CardReqDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.CardResDto;
import com.mokhir.dev.ATM.service.CardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/card")
@RequiredArgsConstructor
public class CardController {
    private final CardService cardService;

    @PostMapping("/create")
    public ResponseEntity<CardResDto> create(@Valid @RequestBody CardReqDto card, HttpServletRequest request) {
        return ResponseEntity.ok().body(cardService.createCard(card, request));
    }

    @GetMapping("/all/pinfl")
    public ResponseEntity<Page<CardResDto>> getAllByPnFl(
            @RequestParam("page") int pageIndex, @RequestParam("size") int pageSize,
            @RequestParam("pinFlNumber") String pinFlNumber,
            HttpServletRequest request
    ){
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        return ResponseEntity.ok().body(cardService.getAllByPnFl(pageable, pinFlNumber, request));
    }

}
