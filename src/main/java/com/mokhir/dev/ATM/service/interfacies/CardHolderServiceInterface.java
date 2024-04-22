package com.mokhir.dev.ATM.service.interfacies;

import jakarta.servlet.http.HttpServletRequest;

public interface CardHolderServiceInterface<Req, Res> {
    Res createCardHolder(Req req, HttpServletRequest servletRequest);
}
