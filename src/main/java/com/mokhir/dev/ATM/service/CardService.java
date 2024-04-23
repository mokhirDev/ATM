package com.mokhir.dev.ATM.service;

import com.google.gson.Gson;
import com.mokhir.dev.ATM.aggregate.dto.req_dto.CardReqDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.CardResDto;
import com.mokhir.dev.ATM.aggregate.entity.Card;
import com.mokhir.dev.ATM.aggregate.entity.CardHolder;
import com.mokhir.dev.ATM.aggregate.entity.CardType;
import com.mokhir.dev.ATM.exceptions.DatabaseException;
import com.mokhir.dev.ATM.exceptions.NotFoundException;
import com.mokhir.dev.ATM.mapper.CardMapper;
import com.mokhir.dev.ATM.repository.CardHolderRepository;
import com.mokhir.dev.ATM.repository.CardRepository;
import com.mokhir.dev.ATM.repository.CardTypeRepository;
import com.mokhir.dev.ATM.repository.CurrencyTypeRepository;
import com.mokhir.dev.ATM.service.interfacies.CardServiceInterface;
import com.mokhir.dev.ATM.service.network.NetworkDataService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class CardService implements CardServiceInterface<CardReqDto, CardResDto> {
    private final Gson gson;
    private final CardMapper cardMapper;
    private final CardRepository cardRepository;
    private final NetworkDataService networkDataService;
    private final CardTypeRepository cardTypeRepository;
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
            Long id = cardReqDto.getUserId();
            Optional<CardHolder> byId = cardHolderRepository.findById(id);
            if (byId.isEmpty()) {
                throw new NotFoundException("Card holder with id:%d not found".formatted(id));
            }
            Long cardTypeId = cardReqDto.getCardTypeId();
            Optional<CardType> byType = cardTypeRepository.findById(cardTypeId);
            if (byType.isEmpty()) {
                throw new NotFoundException("Card type with id:%d not found".formatted(cardTypeId));
            }
            CardType cardType = byType.get();
            CardHolder cardHolder = byId.get();
            entity.setUser(cardHolder);
            entity.setCardExpireDate(
                    LocalDate.now().plusYears(cardType.getExpirationYear()));
            entity.setCardCvc(
                    String.valueOf(ThreadLocalRandom.current().nextInt(111, 999)));
            entity.setCardNumber(
                    cardType.getNumber()+ThreadLocalRandom.current().nextInt(111111, 999999));
            entity.setCardType(cardType);
            cardRepository.save(entity);
            return cardMapper.toDto(entity);
        } catch (Exception ex) {
            LOG.error("CardService: createCard: {}", ex.getMessage());
            throw new DatabaseException("CardService: createCard: " + ex.getMessage());
        }
    }

}
