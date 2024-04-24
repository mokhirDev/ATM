package com.mokhir.dev.ATM.service.interfacies;

import com.mokhir.dev.ATM.aggregate.dto.res_dto.CardResDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CardServiceInterface<Req, Res>{
    Res createCard(Req req, HttpServletRequest servletRequest);
    Page<Res> getAllByPnFl(Pageable pageable, String pnFlNumber, HttpServletRequest request);
}
