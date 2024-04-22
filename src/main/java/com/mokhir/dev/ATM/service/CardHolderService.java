package com.mokhir.dev.ATM.service;

import com.google.gson.Gson;
import com.mokhir.dev.ATM.aggregate.dto.req_dto.CardHolderReqDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.CardHolderResDto;
import com.mokhir.dev.ATM.aggregate.entity.CardHolder;
import com.mokhir.dev.ATM.exceptions.DatabaseException;
import com.mokhir.dev.ATM.mapper.CardHolderMapper;
import com.mokhir.dev.ATM.mapper.CardMapper;
import com.mokhir.dev.ATM.repository.CardHolderRepository;
import com.mokhir.dev.ATM.service.interfacies.CardHolderServiceInterface;
import com.mokhir.dev.ATM.service.network.NetworkDataService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CardHolderService implements CardHolderServiceInterface<CardHolderReqDto, CardHolderResDto> {
    private final Gson gson;
    private final CardHolderMapper cardHolderMapper;
    private final CardHolderRepository cardHolderRepository;
    private final NetworkDataService networkDataService;
    private static final Logger LOG = LoggerFactory.getLogger(CardService.class);

    @Override
    public CardHolderResDto createCardHolder(CardHolderReqDto cardHolderReqDto, HttpServletRequest servletRequest) {
        try {
            String ClientInfo = networkDataService.getClientIPv4Address(servletRequest);
            String ClientIP = networkDataService.getRemoteUserInfo(servletRequest);
            LOG.info("Client host : \t\t {}", gson.toJson(ClientInfo));
            LOG.info("Client IP :  \t\t {}", gson.toJson(ClientIP));
            CardHolder entity = cardHolderMapper.toEntity(cardHolderReqDto);
            CardHolder save = cardHolderRepository.save(entity);
            return cardHolderMapper.toDto(save);
        } catch (Exception ex) {
            LOG.error("CardHolderService: createCardHolder: {}", ex.getMessage());
            throw new DatabaseException("CardHolderService: createCardHolder: " + ex.getMessage());
        }
    }
}
