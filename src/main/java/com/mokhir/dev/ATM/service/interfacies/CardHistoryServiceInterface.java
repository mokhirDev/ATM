package com.mokhir.dev.ATM.service.interfacies;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface CardHistoryServiceInterface<Req, Res> {
    List<Res> getCardHistory(Req req, HttpServletRequest request);
}
