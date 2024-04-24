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
import com.mokhir.dev.ATM.service.interfacies.CardServiceInterface;
import com.mokhir.dev.ATM.service.network.NetworkDataService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
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
            entity.setCardHolder(cardHolder);
            entity.setCheckCardQuantity(3);
            entity.setBalance(0.0);
            entity.setIsActive(true);
            entity.setCardExpireDate(
                    LocalDate.now().plusYears(cardType.getExpirationYear()));
            entity.setCardCvc(
                    String.valueOf(ThreadLocalRandom.current().nextInt(111, 999)));
            entity.setCardNumber(
                    generateCardNumber(cardType.getNumber()));
            entity.setCardType(cardType);
            cardRepository.save(entity);
            LOG.info("Successfully created card:\t\t {}", gson.toJson(entity));
            return cardMapper.toDto(entity);
        } catch (ConstraintViolationException ex) {
            throw new ConstraintViolationException(ex.getConstraintViolations());
        } catch (DataIntegrityViolationException ex) {
            throw new DataIntegrityViolationException(ex.getMessage(), ex.getCause());
        } catch (Exception ex) {
            LOG.error("CardService: createCard: {}", ex.getMessage());
            throw new DatabaseException("CardService: createCard: " + ex.getMessage());
        }
    }

    private String generateCardNumber(String number) {
        long nextLong = new Random().nextLong();
        nextLong = Math.abs(nextLong);
        return number + String.valueOf(nextLong).substring(0, 10);
    }

    @Override
    public Page<CardResDto> getAllByPnFl(Pageable pageable, String pinFlNumber, HttpServletRequest servletRequest) {
        try {
            String ClientInfo = networkDataService.getClientIPv4Address(servletRequest);
            String ClientIP = networkDataService.getRemoteUserInfo(servletRequest);
            LOG.info("Client host : \t\t {}", gson.toJson(ClientInfo));
            LOG.info("Client IP :  \t\t {}", gson.toJson(ClientIP));

            CardHolder byPnFlNumber = cardHolderRepository.findByPinFl(pinFlNumber);
            if (byPnFlNumber == null) {
                throw new NotFoundException("Card holder with PnFil number:%s not found".formatted(pinFlNumber));
            }
            Page<Card> allCardsByCardHolder = cardRepository.findAllCardsByCardHolder(byPnFlNumber, pageable);
            Page<CardResDto> map = allCardsByCardHolder.map(cardMapper::toDto);
            LOG.info("Successfully got cards:\t\t {}", gson.toJson(map));
            return map;
        } catch (Exception ex) {
            throw new DatabaseException("CardService: getAllByPnFl: " + ex.getMessage());
        }
    }

}
