package com.mokhir.dev.ATM.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;
import com.mokhir.dev.ATM.aggregate.dto.req_dto.CashingReqDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.CardHistoryResDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.CardResDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.CashingResDto;
import com.mokhir.dev.ATM.aggregate.entity.*;
import com.mokhir.dev.ATM.exceptions.CardBlockedException;
import com.mokhir.dev.ATM.exceptions.DatabaseException;
import com.mokhir.dev.ATM.exceptions.NotEnoughFundsException;
import com.mokhir.dev.ATM.exceptions.NotFoundException;
import com.mokhir.dev.ATM.mapper.CardHistoryMapper;
import com.mokhir.dev.ATM.repository.BankNoteRepository;
import com.mokhir.dev.ATM.repository.CardRepository;
import com.mokhir.dev.ATM.repository.CashingTypeRepository;
import com.mokhir.dev.ATM.service.interfacies.CashingServiceInterface;
import com.mokhir.dev.ATM.service.network.NetworkDataService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CashingService implements CashingServiceInterface<CashingReqDto, CashingResDto> {
    private final Gson gson;
    private static Long amountCollected = 0L;
    private final CardRepository cardRepository;
    private final CardHistoryService cardHistoryService;
    private final NetworkDataService networkDataService;
    private final BankNoteService bankNoteService;
    private final BankNoteRepository bankNoteRepository;
    private final CashingTypeRepository cashingTypeRepository;
    private final CardHistoryMapper cardHistoryMapper;
    private static final Logger LOG = LoggerFactory.getLogger(CardService.class);


    @Override
    @Transactional
    public CashingResDto cash(CashingReqDto cashingReqDto, HttpServletRequest servletRequest) {
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
                throw new NotEnoughFundsException("You expected amount of cashes: " + amount + ":" + nominalsCount + ":" + amountCollected);
            }
            amountCollected = 0L;
            bankNoteService.excludeNecessaryNominals(nominalsCount);
            HistoryCard cashedCard = cardHistoryService.cashing(byCardNumber, necessaryAmount);
            boolean needCheque = Boolean.parseBoolean(cashingReqDto.getChequeIsNeed());
            return toCashingResDto(cashedCard, needCheque, nominalsCount);
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
                System.out.println("==>Nominal: " + nominal);
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

    private CashingResDto toCashingResDto(
            HistoryCard historyCard, boolean isChequeNeed, Map<Integer, Integer> nominalsCount) {
        CardHistoryResDto cardHistoryResDto = cardHistoryMapper.toDto(historyCard);
        cardHistoryService.maskingCardHistory(cardHistoryResDto);
        CardResDto fromCard = cardHistoryResDto.getFromCard();
        CashingResDto cashingResDto = CashingResDto.builder()
                .id(cardHistoryResDto.getId())
                .transactionTime(cardHistoryResDto.getDate())
                .message("Cashed successfully")
                .build();

        if (isChequeNeed) {
            cashingResDto.setChequeIsNeed(true);
            cashingResDto.setAmount(String.valueOf(cardHistoryResDto.getAmount()));
            cashingResDto.setCardNumber(fromCard.getCardNumber());
            cashingResDto.setCashingTypeId(fromCard.getCardType().getId());
            cashingResDto.setCashedNominals(nominalsCount);
            cashingResDto.setCardHolderResDto(fromCard.getCardHolder());
        }
        return cashingResDto;
    }
}
