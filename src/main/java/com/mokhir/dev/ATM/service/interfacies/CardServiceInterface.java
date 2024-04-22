package com.mokhir.dev.ATM.service.interfacies;

import jakarta.servlet.http.HttpServletRequest;

public interface CardServiceInterface<Req, Res>{
    Res createCard(Req req, HttpServletRequest servletRequest);
}
