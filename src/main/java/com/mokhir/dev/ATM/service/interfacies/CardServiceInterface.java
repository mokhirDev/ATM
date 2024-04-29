package com.mokhir.dev.ATM.service.interfacies;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CardServiceInterface<Req, Res>{
    Res createCard(Req req, HttpServletRequest servletRequest);
    Page<Res> getAllByPnFl(Pageable pageable, String pnFlNumber, HttpServletRequest request);
}
