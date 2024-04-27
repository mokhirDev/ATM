package com.mokhir.dev.ATM.service;

import com.google.gson.Gson;
import com.mokhir.dev.ATM.aggregate.dto.req_dto.CardHistoryReqDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.CardHistoryResDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.CardHolderResDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.CardResDto;
import com.mokhir.dev.ATM.aggregate.entity.Card;
import com.mokhir.dev.ATM.aggregate.entity.HistoryCard;
import com.mokhir.dev.ATM.exceptions.DatabaseException;
import com.mokhir.dev.ATM.mapper.CardHistoryMapper;
import com.mokhir.dev.ATM.mapper.CardHolderMapper;
import com.mokhir.dev.ATM.mapper.CardMapper;
import com.mokhir.dev.ATM.repository.BankNoteRepository;
import com.mokhir.dev.ATM.repository.CardHistoryRepository;
import com.mokhir.dev.ATM.repository.CardRepository;
import com.mokhir.dev.ATM.service.interfacies.CardHistoryServiceInterface;
import com.mokhir.dev.ATM.service.network.NetworkDataService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CardHistoryService implements CardHistoryServiceInterface<CardHistoryReqDto, CardHistoryResDto> {
    private final Gson gson;
    private final NetworkDataService networkDataService;
    private final CardHistoryRepository cardHistoryRepository;
    private static final Logger LOG = LoggerFactory.getLogger(CardService.class);
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final CardHistoryMapper cardHistoryMapper;
    private final CardHolderMapper cardHolderMapper;
    private final BankNoteRepository bankNoteRepository;

    @Override
    @Transactional
    public List<CardHistoryResDto> getCardHistory(CardHistoryReqDto cardHistoryReqDto, HttpServletRequest servletRequest) {
        try {
            String ClientInfo = networkDataService.getClientIPv4Address(servletRequest);
            String ClientIP = networkDataService.getRemoteUserInfo(servletRequest);
            LOG.info("Client host : \t\t {}", gson.toJson(ClientInfo));
            LOG.info("Client IP :  \t\t {}", gson.toJson(ClientIP));

            Long cardId = cardHistoryReqDto.getIdFrom();
            Optional<Card> byId = cardRepository.findById(cardId);
            if (byId.isEmpty()) {
                throw new DatabaseException("Card not found, with id: %d".formatted(cardId));
            }
            Card card = byId.get();
            List<HistoryCard> allByFromCardOrToCard = cardHistoryRepository.findAllByFromCardOrToCard(card, card);
            return doAnonymousCardHistoryRes(allByFromCardOrToCard);
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public HistoryCard cashing(Long amount) {
        try {
            BigDecimal amountCashing = BigDecimal.valueOf(amount);
            BigDecimal cashingCommission = amountCashing.multiply(BigDecimal.valueOf(0.01));
            HistoryCard historyCard = HistoryCard.builder()
                    .amount(amountCashing)
                    .date(LocalDateTime.now())
                    .commission(cashingCommission)
                    .build();
            cardHistoryRepository.save(historyCard);
            return historyCard;
        } catch (Exception ex) {
            throw new DatabaseException(ex.getMessage());
        }
    }

    private List<CardHistoryResDto> doAnonymousCardHistoryRes(List<HistoryCard> historyCards) {
        historyCards.forEach(cardHistory -> {
            CardHolderResDto cardHolderFromRes = cardHolderMapper.toDto(cardHistory
                    .getFromCard()
                    .getCardHolder());
            CardHolderResDto cardHolderToRes = cardHolderMapper.toDto(cardHistory
                    .getToCard()
                    .getCardHolder());

            CardResDto fromCardRes = cardMapper.toDto(cardHistory.getFromCard());
            CardResDto toCardRes = cardMapper.toDto(cardHistory.getToCard());

            fromCardRes.setCardHolder(cardHolderFromRes);
            toCardRes.setCardHolder(cardHolderToRes);
        });

        List<CardHistoryResDto> cardHistoryResDtoList = historyCards
                .stream()
                .map(cardHistoryMapper::toDto)
                .toList();

        cardHistoryResDtoList.forEach(this::maskingCardHistory);
        return cardHistoryResDtoList;

    }

    public void maskingCardHistory(CardHistoryResDto cardHistoryResDto) {
        //getting card of sender
        CardResDto senderCard = cardHistoryResDto.getFromCard();
        //masking sender
        cardHistoryResDto.setFromCard(maskCard(senderCard));

        //getting card of receiver
        CardResDto receiverCard = cardHistoryResDto.getToCard();
        //masking receiver
        cardHistoryResDto.setToCard(maskCard(receiverCard));

    }

    public CardResDto maskCard(CardResDto cardResDto) {
        if (cardResDto == null) {
            return null;
        }
        //getting card number
        String cardNumber = cardResDto.getCardNumber();
        //doing anonymous card number
        String maskedCardNumber = cardNumber
                .substring(0, 6) + "******" + cardNumber.substring(12);
        cardResDto.setCardNumber(maskedCardNumber);

        //getting cardholder
        CardHolderResDto cardHolder = cardResDto.getCardHolder();
        //getting phone number
        String phoneNumber = cardHolder.getPhoneNumber();
        //doing anonymous phone number
        String maskedPhoneNumber = phoneNumber
                .substring(0, 4) + "****" + phoneNumber.substring(8);
        cardHolder.setPhoneNumber(maskedPhoneNumber);

        //getting cardholder name
        String name = cardHolder.getName();
        //masking of sender name
        String maskedName = maskString(name);
        cardHolder.setName(maskedName);

        //getting cardholder lastname
        String lastName = cardHolder.getLastName();
        //masking lastname
        String maskedLastName = maskString(lastName);
        cardHolder.setLastName(maskedLastName);
        return cardResDto;
    }


    private String maskString(String str) {
        if (str == null || str.length() <= 2) {
            return str;
        }
        char[] chars = str.toCharArray();
        for (int i = 1; i < chars.length - 1; i++) {
            chars[i] = '*';
        }
        return new String(chars);
    }


    public HistoryCard fill(Card fillingCard, long amount,
                            HttpServletRequest servletRequest) {
        String ClientInfo = networkDataService.getClientIPv4Address(servletRequest);
        String ClientIP = networkDataService.getRemoteUserInfo(servletRequest);
        LOG.info("Client host : \t\t {}", gson.toJson(ClientInfo));
        LOG.info("Client IP :  \t\t {}", gson.toJson(ClientIP));

        BigDecimal amountCashing = BigDecimal.valueOf(amount);
        BigDecimal cashingCommission = amountCashing.multiply(BigDecimal.valueOf(0.01));
        HistoryCard historyCard = HistoryCard.builder()
                .amount(amountCashing)
                .date(LocalDateTime.now())
                .toCard(fillingCard)
                .date(LocalDateTime.now())
                .commission(cashingCommission)
                .build();
        cardHistoryRepository.save(historyCard);
        return historyCard;
    }

    public HistoryCard transform(Card sender, Card receiver, long transformBalance) {
        BigDecimal amountCashing = BigDecimal.valueOf(transformBalance);
        BigDecimal cashingCommission = amountCashing.multiply(BigDecimal.valueOf(0.01));
        HistoryCard historyCard = HistoryCard.builder()
                .amount(amountCashing)
                .date(LocalDateTime.now())
                .fromCard(sender)
                .toCard(receiver)
                .commission(cashingCommission)
                .build();
        cardHistoryRepository.save(historyCard);

        return historyCard;
    }
}
