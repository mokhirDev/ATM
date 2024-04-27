package com.mokhir.dev.ATM.service.interfacies;

import com.mokhir.dev.ATM.aggregate.dto.req_dto.CashingReqDto;
import com.mokhir.dev.ATM.aggregate.dto.req_dto.FillingReqDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.ChequeResDto;
import jakarta.servlet.http.HttpServletRequest;

public interface CardTransactionServiceInterface {
    ChequeResDto cash(CashingReqDto req, HttpServletRequest servletRequest);
    ChequeResDto fill(FillingReqDto req, HttpServletRequest servletRequest);
}
