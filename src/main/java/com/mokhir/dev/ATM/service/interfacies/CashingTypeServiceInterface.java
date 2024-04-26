package com.mokhir.dev.ATM.service.interfacies;

import jakarta.servlet.http.HttpServletRequest;

public interface CashingTypeServiceInterface<Req, Res> {
    Res create(Req req, HttpServletRequest servletRequest);
}
