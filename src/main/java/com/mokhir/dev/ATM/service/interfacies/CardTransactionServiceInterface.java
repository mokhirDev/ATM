package com.mokhir.dev.ATM.service.interfacies;

import com.mokhir.dev.ATM.aggregate.dto.req_dto.CashingReqDto;
import com.mokhir.dev.ATM.aggregate.dto.req_dto.FillingReqDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.CashingResDto;
import jakarta.servlet.http.HttpServletRequest;

public interface CardTransactionServiceInterface {
    CashingResDto cash(CashingReqDto req, HttpServletRequest servletRequest);
    CashingResDto fill(FillingReqDto req, HttpServletRequest servletRequest);
}
