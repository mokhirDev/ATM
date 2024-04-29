package com.mokhir.dev.ATM.service.interfacies;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CardHistoryServiceInterface<Req, Res> {
    Page<Res> getCardHistory(Req req, HttpServletRequest request, Pageable pageable);
}
