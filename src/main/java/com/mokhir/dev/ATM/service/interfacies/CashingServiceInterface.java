package com.mokhir.dev.ATM.service.interfacies;

import jakarta.servlet.http.HttpServletRequest;

public interface CashingServiceInterface <Req, Res>{
    Res cash(Req req, HttpServletRequest servletRequest);
}
