package com.mokhir.dev.ATM.service;

import com.google.gson.Gson;
import com.mokhir.dev.ATM.aggregate.dto.req_dto.CardReqDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.CardResDto;
import com.mokhir.dev.ATM.aggregate.entity.Card;
import com.mokhir.dev.ATM.aggregate.entity.CardHolder;
import com.mokhir.dev.ATM.exceptions.DatabaseException;
import com.mokhir.dev.ATM.exceptions.NotFoundException;
import com.mokhir.dev.ATM.mapper.CardMapper;
import com.mokhir.dev.ATM.repository.CardHolderRepository;
import com.mokhir.dev.ATM.repository.CardRepository;
import com.mokhir.dev.ATM.service.interfacies.CardServiceInterface;
import com.mokhir.dev.ATM.service.network.NetworkDataService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CardService implements CardServiceInterface<CardReqDto, CardResDto> {
    private final Gson gson;
    private final CardMapper cardMapper;
    private final CardRepository cardRepository;
    private final NetworkDataService networkDataService;
    private final CardHolderRepository cardHolderRepository;
    private static final Logger LOG = LoggerFactory.getLogger(CardService.class);


    @Override
    public CardResDto createCard(CardReqDto cardReqDto, HttpServletRequest servletRequest) {
        try {
            String ClientInfo = networkDataService.getClientIPv4Address(servletRequest);
            String ClientIP = networkDataService.getRemoteUserInfo(servletRequest);
            LOG.info("Client host : \t\t {}", gson.toJson(ClientInfo));
            LOG.info("Client IP :  \t\t {}", gson.toJson(ClientIP));

            Card entity = cardMapper.toEntity(cardReqDto);
            Long id = cardReqDto.getUser().getId();
            Optional<CardHolder> byId = cardHolderRepository.findById(id);
            if (byId.isEmpty()) {
                throw new NotFoundException("Card holder with id:%d not found".formatted(id));
            }
            CardHolder cardHolder = byId.get();
            entity.setUser(cardHolder);
            Card cardSave = cardRepository.save(entity);
            return cardMapper.toDto(cardSave);
        } catch (Exception ex) {
            LOG.error("CardService: createCard: {}", ex.getMessage());
            throw new DatabaseException("CardService: createCard: " + ex.getMessage());
        }
    }

}
