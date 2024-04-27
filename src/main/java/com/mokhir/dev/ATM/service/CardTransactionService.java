package com.mokhir.dev.ATM.service;

import com.google.gson.Gson;
import com.mokhir.dev.ATM.aggregate.dto.req_dto.CardTransformReqDto;
import com.mokhir.dev.ATM.aggregate.dto.req_dto.CashingReqDto;
import com.mokhir.dev.ATM.aggregate.dto.req_dto.FillingReqDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.CardHistoryResDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.CardResDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.ChequeResDto;
import com.mokhir.dev.ATM.aggregate.entity.*;
import com.mokhir.dev.ATM.exceptions.*;
import com.mokhir.dev.ATM.mapper.CardHistoryMapper;
import com.mokhir.dev.ATM.mapper.CardMapper;
import com.mokhir.dev.ATM.repository.BankNoteRepository;
import com.mokhir.dev.ATM.repository.CardRepository;
import com.mokhir.dev.ATM.repository.CashingTypeRepository;
import com.mokhir.dev.ATM.repository.CurrencyTypeRepository;
import com.mokhir.dev.ATM.service.interfacies.CardTransactionServiceInterface;
import com.mokhir.dev.ATM.service.network.NetworkDataService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CardTransactionService implements CardTransactionServiceInterface {
    private final Gson gson;
    private static Long amountCollected = 0L;
    private final CardRepository cardRepository;
    private final CardHistoryService cardHistoryService;
    private final NetworkDataService networkDataService;
    private final BankNoteService bankNoteService;
    private final BankNoteRepository bankNoteRepository;
    private final CashingTypeRepository cashingTypeRepository;
    private final CardHistoryMapper cardHistoryMapper;
    private final CardMapper cardMapper;
    private static final Logger LOG = LoggerFactory.getLogger(CardService.class);
    private final CurrencyTypeRepository currencyTypeRepository;


    @Override
    @Transactional
    public ChequeResDto cash(CashingReqDto cashingReqDto, HttpServletRequest servletRequest) {
        try {
            String ClientInfo = networkDataService.getClientIPv4Address(servletRequest);
            String ClientIP = networkDataService.getRemoteUserInfo(servletRequest);
            LOG.info("Client host : \t\t {}", gson.toJson(ClientInfo));
            LOG.info("Client IP :  \t\t {}", gson.toJson(ClientIP));

            String cardNumber = cashingReqDto.getCardNumber();
            Card byCardNumber = cardRepository.findByCardNumber(cardNumber);
            if (!byCardNumber.getIsActive()) {
                throw new CardBlockedException("Please try again, after unblocking your card!");
            }
            String reqDtoCardPin = cashingReqDto.getCardPin();
            String originalCardPin = String.valueOf(byCardNumber.getCardPin());
            if (!reqDtoCardPin.equals(originalCardPin)) {
                throw new NotFoundException("Card pin does not match");
            }
            long amount = Long.parseLong(cashingReqDto.getAmount());
            long necessaryAmount = (long) (amount * 0.01 + amount);

            if (!(necessaryAmount <= byCardNumber.getBalance())) {
                throw new NotEnoughFundsException("Not enough funds in your card");
            }

            Map<Integer, Integer> nominalsCount = getNominalsCount(cashingReqDto, byCardNumber);
            nominalsCount.entrySet().removeIf(entry -> entry.getValue() == 0);

            if (!amountCollected.equals(amount)) {
                throw new NotEnoughFundsException
                        ("You expected amount of cashes: " + amount + ":" + nominalsCount + ":" + amountCollected);
            }
            amountCollected = 0L;
            bankNoteService.excludeNecessaryNominals(nominalsCount);
            HistoryCard cashedCard = cardHistoryService.cashing(necessaryAmount);
            BigDecimal sumCashing = cashedCard.getAmount().add(cashedCard.getCommission());
            byCardNumber.setBalance(byCardNumber.getBalance() - Double.parseDouble(String.valueOf(sumCashing)));
            cashedCard.setFromCard(byCardNumber);
            cashedCard.setToCard(null);
            boolean needCheque = Boolean.parseBoolean(cashingReqDto.getChequeIsNeed());
            ChequeResDto chequeResDto = toCashingResDto(cashedCard, needCheque, nominalsCount);
            chequeResDto.setCashingTypeId(Long.valueOf(cashingReqDto.getCashingTypeId()));
            return chequeResDto;
        } catch (Exception ex) {
            throw new DatabaseException(ex.getMessage());
        }
    }


    @Override
    @Transactional
    public ChequeResDto fill(FillingReqDto fillingReqDto, HttpServletRequest servletRequest) {
        try {
            String ClientInfo = networkDataService.getClientIPv4Address(servletRequest);
            String ClientIP = networkDataService.getRemoteUserInfo(servletRequest);
            LOG.info("Client host : \t\t {}", gson.toJson(ClientInfo));
            LOG.info("Client IP :  \t\t {}", gson.toJson(ClientIP));

            //validation of card
            String cardNumber = fillingReqDto.getCardNumber();
            Card byCardNumber = cardRepository.findByCardNumber(cardNumber);
            if (!byCardNumber.getIsActive()) {
                throw new CardBlockedException("Please try again, after unblocking your card!");
            }
            String reqDtoCardPin = fillingReqDto.getCardPin();
            String originalCardPin = String.valueOf(byCardNumber.getCardPin());
            if (!reqDtoCardPin.equals(originalCardPin)) {
                throw new NotFoundException("Card pin does not match");
            }

            long amount = Long.parseLong(fillingReqDto.getAmount());
            double fillingBalance = amount - amount * 0.01;
            //filling balance with commission
            byCardNumber.setBalance(byCardNumber.getBalance() + fillingBalance);
            cardRepository.save(byCardNumber);
            HistoryCard historyCard = cardHistoryService.fill(byCardNumber, amount, servletRequest);


            boolean needCheque = Boolean.parseBoolean(fillingReqDto.getChequeIsNeed());
            CardResDto cardResDto = cardMapper.toDto(historyCard.getToCard());
            CardHistoryResDto cardHistoryResDto = CardHistoryResDto
                    .builder()
                    .date(historyCard.getDate())
                    .id(historyCard.getId())
                    .commission(historyCard.getCommission())
                    .toCard(cardResDto)
                    .amount(historyCard.getAmount())
                    .build();
            cardHistoryService.maskingCardHistory(cardHistoryResDto);
            CardResDto toCard = cardHistoryResDto.getToCard();
            ChequeResDto chequeResDto = ChequeResDto.builder()
                    .id(cardHistoryResDto.getId())
                    .transactionTime(cardHistoryResDto.getDate())
                    .message("Cashed successfully")
                    .build();

            if (needCheque) {
                chequeResDto.setChequeIsNeed(true);
                chequeResDto.setAmount(String.valueOf(fillingBalance));
                chequeResDto.setCommission(String.valueOf(amount * 0.01));
                chequeResDto.setSenderCardNumber(toCard.getCardNumber());
                chequeResDto.setCardHolderSenderDto(toCard.getCardHolder());
            }
            return chequeResDto;
        } catch (Exception ex) {
            throw new DatabaseException(ex.getMessage());
        }
    }


    public ChequeResDto transform(CardTransformReqDto transformReq, HttpServletRequest servletRequest) {
        try {
            String ClientInfo = networkDataService.getClientIPv4Address(servletRequest);
            String ClientIP = networkDataService.getRemoteUserInfo(servletRequest);
            LOG.info("Client host : \t\t {}", gson.toJson(ClientInfo));
            LOG.info("Client IP :  \t\t {}", gson.toJson(ClientIP));

            //validation of cards
            String cardNumberSender = transformReq.getCardFrom();
            String cardNumberReceiver = transformReq.getCardTo();
            Card byCardNumberSender = cardRepository.findByCardNumber(cardNumberSender);
            Card byCardNumberReceiver = cardRepository.findByCardNumber(cardNumberReceiver);
            if (!byCardNumberSender.getIsActive() || !byCardNumberReceiver.getIsActive()) {
                throw new CardBlockedException("Please try again, after unblocking your card!");
            }
            String reqDtoCardPin = transformReq.getCardPin();
            String originalCardPin = String.valueOf(byCardNumberSender.getCardPin());
            if (!reqDtoCardPin.equals(originalCardPin)) {
                throw new NotFoundException("Card pin does not match");
            }

            if (byCardNumberSender.getCardNumber().equals(byCardNumberReceiver.getCardNumber())){
                throw new SelfTransactionException("You can't transform from the current card to the same card!");
            }

            if (!byCardNumberSender.getCardType().getCurrencyType().getId()
                    .equals(byCardNumberReceiver.getCardType().getCurrencyType().getId())){
                throw new SelfTransactionException("You can't transform to another card type");
            }

            long transformBalance = Long.parseLong(transformReq.getAmount());
            Double minusAmount = Double.parseDouble(String.valueOf(transformBalance + transformBalance * 0.01));
            if (!(byCardNumberSender.getBalance() >= minusAmount)) {
                throw new NotEnoughFundsException("Not enough funds, in your balance");
            }

            byCardNumberSender.setBalance(byCardNumberSender.getBalance() - minusAmount);
            //filling balance with commission
            byCardNumberReceiver.setBalance(byCardNumberReceiver.getBalance() + transformBalance);
            cardRepository.save(byCardNumberSender);
            cardRepository.save(byCardNumberReceiver);
            HistoryCard historyCard = cardHistoryService
                    .transform(byCardNumberSender, byCardNumberReceiver, transformBalance);


            boolean needCheque = Boolean.parseBoolean(transformReq.getChequeIsNeed());
            CardResDto cardResSender = cardMapper.toDto(historyCard.getToCard());
            CardResDto cardResReceiver = cardMapper.toDto(historyCard.getFromCard());
            CardHistoryResDto cardHistoryResDto = CardHistoryResDto
                    .builder()
                    .date(historyCard.getDate())
                    .id(historyCard.getId())
                    .commission(historyCard.getCommission())
                    .toCard(cardResReceiver)
                    .fromCard(cardResSender)
                    .amount(historyCard.getAmount())
                    .build();
            cardHistoryService.maskingCardHistory(cardHistoryResDto);
            ChequeResDto chequeResDto = ChequeResDto.builder()
                    .id(cardHistoryResDto.getId())
                    .transactionTime(cardHistoryResDto.getDate())
                    .message("Sent successfully")
                    .build();

            if (needCheque) {
                cardHistoryService.maskCard(cardResSender);
                cardHistoryService.maskCard(cardResReceiver);
                chequeResDto.setChequeIsNeed(true);
                chequeResDto.setAmount(String.valueOf(transformBalance));
                chequeResDto.setCommission(String.valueOf(transformBalance * 0.01));
                chequeResDto.setCardHolderSenderDto(cardResSender.getCardHolder());
                chequeResDto.setCardHolderReceiverDto(cardResReceiver.getCardHolder());
                chequeResDto.setSenderCardNumber(cardResSender.getCardNumber());
                chequeResDto.setReceiverCardNumber(cardResReceiver.getCardNumber());
            }
            return chequeResDto;
        } catch (Exception ex) {
            throw new DatabaseException(ex.getMessage());
        }
    }

    private Map<Integer, Integer> getNominalsCount(CashingReqDto cashingReqDto, Card card) {
        try {
            amountCollected = 0L;
            List<Integer> bankNotesList = new ArrayList<>();
            List<Integer> quantitiesList = new ArrayList<>();
            String cashingTypeId = cashingReqDto.getCashingTypeId();
            String cashingAmount = cashingReqDto.getAmount();
            Optional<CashingType> byId = cashingTypeRepository.findById(Long.valueOf(cashingTypeId));
            if (byId.isEmpty()) {
                throw new NotFoundException("cashingType not found, with id : " + cashingTypeId);
            }
            CashingType cashingType = byId.get();
            CurrencyType currencyType = card.getCardType().getCurrencyType();
            List<BanknoteType> banknoteTypes = bankNoteRepository.findAllByCashingTypeId(cashingType);
            if (banknoteTypes.isEmpty()) {
                throw new NotFoundException("No banknotes found");
            }
            banknoteTypes = banknoteTypes.stream().filter(b -> b.getCurrencyTypeId().equals(currencyType)).toList();
            banknoteTypes = banknoteTypes.stream().sorted().toList();
            banknoteTypes.forEach(bankNote -> {
                bankNotesList.add(bankNote.getNominal());
                quantitiesList.add(bankNote.getQuantity());
            });
            return getCashingNominalsByType(bankNotesList, quantitiesList, Long.parseLong(cashingAmount));
        } catch (Exception ex) {
            throw new DatabaseException("CashingService: getNominalsCount: " + ex.getMessage());
        }
    }


    private Map<Integer, Integer> getCashingNominalsByType
            (List<Integer> nominals, List<Integer> quantities, Long requiredCashingAmount) {
        Map<Integer, Integer> cashingNominals = new HashMap<>();
        for (int i = 0; i < nominals.size(); i++) {
            Integer nominal = nominals.get(i);
            int count = 1;
            for (int j = 0; j < quantities.get(i); j++) {
                cashingNominals.put(nominal, count);
                amountCollected += nominal;
                if (Objects.equals(amountCollected, requiredCashingAmount)) {
                    return cashingNominals;
                }
                if (amountCollected > requiredCashingAmount) {
                    amountCollected -= nominal;
                    if (amountCollected < requiredCashingAmount) {
                        count--;
                        cashingNominals.put(nominal, count);
                        break;
                    } else {
                        return cashingNominals;
                    }
                }
                count++;
            }
        }
        return cashingNominals;
    }

    private ChequeResDto toCashingResDto(
            HistoryCard historyCard, boolean isChequeNeed, Map<Integer, Integer> nominalsCount) {
        CardHistoryResDto cardHistoryResDto = cardHistoryMapper.toDto(historyCard);
        cardHistoryService.maskingCardHistory(cardHistoryResDto);
        CardResDto fromCard = cardHistoryResDto.getFromCard();
        ChequeResDto chequeResDto = ChequeResDto.builder()
                .id(cardHistoryResDto.getId())
                .transactionTime(cardHistoryResDto.getDate())
                .message("Cashed successfully")
                .build();

        if (isChequeNeed) {
            chequeResDto.setChequeIsNeed(true);
            chequeResDto.setAmount(String.valueOf(cardHistoryResDto.getAmount()));
            chequeResDto.setSenderCardNumber(fromCard.getCardNumber());
            chequeResDto.setCashedNominals(nominalsCount);
            chequeResDto.setCommission(String.valueOf(historyCard.getCommission()));
            chequeResDto.setCardHolderSenderDto(fromCard.getCardHolder());
        }
        return chequeResDto;
    }
}
