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


    /**
     * Processes a cash withdrawal request.
     *
     * @param cashingReqDto   The DTO containing the information for the cash withdrawal request.
     * @param servletRequest  The servlet request object to extract client information.
     * @return                The DTO representing the cash withdrawal response.
     * @throws DatabaseException if an error occurs while interacting with the database.
     */
    @Override
    @Transactional
    public ChequeResDto cash(CashingReqDto cashingReqDto, HttpServletRequest servletRequest) {
        try {
            // Retrieve client information from the servlet request
            String clientInfo = networkDataService.getClientIPv4Address(servletRequest);
            String clientIP = networkDataService.getRemoteUserInfo(servletRequest);
            // Log client information
            LOG.info("Client host: {}", gson.toJson(clientInfo));
            LOG.info("Client IP: {}", gson.toJson(clientIP));

            // Retrieve card number from DTO
            String cardNumber = cashingReqDto.getCardNumber();
            // Find the card by card number
            Card byCardNumber = cardRepository.findByCardNumber(cardNumber);
            // Check if the card is active
            if (!byCardNumber.getIsActive()) {
                throw new CardBlockedException("Please try again, after unblocking your card!");
            }

            // Retrieve card PIN from DTO
            String reqDtoCardPin = cashingReqDto.getCardPin();
            // Retrieve original card PIN from the card entity
            String originalCardPin = String.valueOf(byCardNumber.getCardPin());
            // Compare PINs
            if (!reqDtoCardPin.equals(originalCardPin)) {
                throw new NotFoundException("Card pin does not match");
            }

            // Calculate necessary amount for withdrawal
            long amount = Long.parseLong(cashingReqDto.getAmount());
            long necessaryAmount = (long) (amount * 0.01 + amount);

            // Check if there are enough funds in the card
            if (!(necessaryAmount <= byCardNumber.getBalance())) {
                throw new NotEnoughFundsException("Not enough funds in your card");
            }

            // Get nominal counts for cash withdrawal
            Map<Integer, Integer> nominalsCount = getNominalsCount(cashingReqDto, byCardNumber);
            nominalsCount.entrySet().removeIf(entry -> entry.getValue() == 0);

            // Validate collected amount
            if (!amountCollected.equals(amount)) {
                throw new NotEnoughFundsException("You expected amount of cashes: " + amount + ":" + nominalsCount + ":" + amountCollected);
            }
            amountCollected = 0L;

            // Exclude necessary nominals from banknotes
            bankNoteService.excludeNecessaryNominals(nominalsCount);

            // Perform cashing operation and update card balance
            HistoryCard cashedCard = cardHistoryService.cashing(necessaryAmount);
            BigDecimal sumCashing = cashedCard.getAmount().add(cashedCard.getCommission());
            byCardNumber.setBalance(byCardNumber.getBalance() - Double.parseDouble(String.valueOf(sumCashing)));
            cashedCard.setFromCard(byCardNumber);
            cashedCard.setToCard(null);

            // Check if a cheque is needed
            boolean needCheque = Boolean.parseBoolean(cashingReqDto.getChequeIsNeed());
            ChequeResDto chequeResDto = toCashingResDto(cashedCard, needCheque, nominalsCount);
            chequeResDto.setCashingTypeId(Long.valueOf(cashingReqDto.getCashingTypeId()));
            return chequeResDto;
        } catch (Exception ex) {
            // In case of an error, throw an exception with a database error message
            throw new DatabaseException(ex.getMessage());
        }
    }


    /**
     * Processes a cash filling request.
     *
     * @param fillingReqDto   The DTO containing the information for the cash filling request.
     * @param servletRequest  The servlet request object to extract client information.
     * @return                The DTO representing the cash filling response.
     * @throws DatabaseException if an error occurs while interacting with the database.
     */
    @Override
    @Transactional
    public ChequeResDto fill(FillingReqDto fillingReqDto, HttpServletRequest servletRequest) {
        try {
            // Retrieve client information from the servlet request
            String clientInfo = networkDataService.getClientIPv4Address(servletRequest);
            String clientIP = networkDataService.getRemoteUserInfo(servletRequest);
            // Log client information
            LOG.info("Client host: {}", gson.toJson(clientInfo));
            LOG.info("Client IP: {}", gson.toJson(clientIP));

            // Validate card
            String cardNumber = fillingReqDto.getCardNumber();
            Card byCardNumber = cardRepository.findByCardNumber(cardNumber);
            // Check if the card is active
            if (!byCardNumber.getIsActive()) {
                throw new CardBlockedException("Please try again, after unblocking your card!");
            }

            // Validate card PIN
            String reqDtoCardPin = fillingReqDto.getCardPin();
            String originalCardPin = String.valueOf(byCardNumber.getCardPin());
            // Compare PINs
            if (!reqDtoCardPin.equals(originalCardPin)) {
                throw new NotFoundException("Card pin does not match");
            }

            // Calculate amount and filling balance
            long amount = Long.parseLong(fillingReqDto.getAmount());
            double fillingBalance = amount - amount * 0.01;
            // Fill card balance with commission
            byCardNumber.setBalance(byCardNumber.getBalance() + fillingBalance);
            cardRepository.save(byCardNumber);

            // Perform filling operation and generate history card
            HistoryCard historyCard = cardHistoryService.fill(byCardNumber, amount, servletRequest);

            // Check if a cheque is needed
            boolean needCheque = Boolean.parseBoolean(fillingReqDto.getChequeIsNeed());
            ChequeResDto chequeResDto = ChequeResDto.builder()
                    .id(historyCard.getId())
                    .transactionTime(historyCard.getDate())
                    .message("Cashed successfully")
                    .build();

            // If cheque is needed, populate cheque details
            if (needCheque) {
                chequeResDto.setChequeIsNeed(true);
                chequeResDto.setAmount(String.valueOf(fillingBalance));
                chequeResDto.setCommission(String.valueOf(amount * 0.01));
                // Set sender card details
                CardResDto cardResDto = cardMapper.toDto(historyCard.getToCard());
                chequeResDto.setSenderCardNumber(cardResDto.getCardNumber());
                chequeResDto.setCardHolderSenderDto(cardResDto.getCardHolder());
            }
            return chequeResDto;
        } catch (Exception ex) {
            // In case of an error, throw an exception with a database error message
            throw new DatabaseException(ex.getMessage());
        }
    }



    /**
     * Transforms funds from one card to another.
     *
     * @param transformReq   The DTO containing the information for the funds transformation request.
     * @param servletRequest The servlet request object to extract client information.
     * @return               The DTO representing the transformation response.
     * @throws DatabaseException if an error occurs while interacting with the database.
     */
    public ChequeResDto transform(CardTransformReqDto transformReq, HttpServletRequest servletRequest) {
        try {
            // Retrieve client information from the servlet request
            String clientInfo = networkDataService.getClientIPv4Address(servletRequest);
            String clientIP = networkDataService.getRemoteUserInfo(servletRequest);
            // Log client information
            LOG.info("Client host: {}", gson.toJson(clientInfo));
            LOG.info("Client IP: {}", gson.toJson(clientIP));

            // Validate sender and receiver cards
            String cardNumberSender = transformReq.getCardFrom();
            String cardNumberReceiver = transformReq.getCardTo();
            Card byCardNumberSender = cardRepository.findByCardNumber(cardNumberSender);
            Card byCardNumberReceiver = cardRepository.findByCardNumber(cardNumberReceiver);
            // Check if the cards are active
            if (!byCardNumberSender.getIsActive() || !byCardNumberReceiver.getIsActive()) {
                throw new CardBlockedException("Please try again, after unblocking your card!");
            }

            // Validate card PIN
            String reqDtoCardPin = transformReq.getCardPin();
            String originalCardPin = String.valueOf(byCardNumberSender.getCardPin());
            // Compare PINs
            if (!reqDtoCardPin.equals(originalCardPin)) {
                throw new NotFoundException("Card pin does not match");
            }

            // Ensure sender and receiver are different cards
            if (byCardNumberSender.getCardNumber().equals(byCardNumberReceiver.getCardNumber())){
                throw new SelfTransactionException("You can't transform from the current card to the same card!");
            }

            // Ensure sender and receiver have the same currency type
            if (!byCardNumberSender.getCardType().getCurrencyType().getId()
                    .equals(byCardNumberReceiver.getCardType().getCurrencyType().getId())){
                throw new SelfTransactionException("You can't transform to another card type");
            }

            // Calculate transformed balance
            long transformBalance = Long.parseLong(transformReq.getAmount());
            Double minusAmount = Double.parseDouble(String.valueOf(transformBalance + transformBalance * 0.01));
            // Check if sender has sufficient funds
            if (!(byCardNumberSender.getBalance() >= minusAmount)) {
                throw new NotEnoughFundsException("Not enough funds, in your balance");
            }

            // Perform the transformation
            byCardNumberSender.setBalance(byCardNumberSender.getBalance() - minusAmount);
            // Fill receiver's balance with the transformed amount
            byCardNumberReceiver.setBalance(byCardNumberReceiver.getBalance() + transformBalance);
            cardRepository.save(byCardNumberSender);
            cardRepository.save(byCardNumberReceiver);
            // Generate history card for the transformation
            HistoryCard historyCard = cardHistoryService
                    .transform(byCardNumberSender, byCardNumberReceiver, transformBalance);

            // Check if a cheque is needed
            boolean needCheque = Boolean.parseBoolean(transformReq.getChequeIsNeed());
            // Map sender and receiver card details to DTOs
            CardResDto cardResSender = cardMapper.toDto(historyCard.getToCard());
            CardResDto cardResReceiver = cardMapper.toDto(historyCard.getFromCard());
            // Build card history response DTO
            CardHistoryResDto cardHistoryResDto = CardHistoryResDto
                    .builder()
                    .date(historyCard.getDate())
                    .id(historyCard.getId())
                    .commission(historyCard.getCommission())
                    .toCard(cardResReceiver)
                    .fromCard(cardResSender)
                    .amount(historyCard.getAmount())
                    .build();
            // Mask sensitive information in card history response DTO
            cardHistoryService.maskingCardHistory(cardHistoryResDto);
            // Build cheque response DTO
            ChequeResDto chequeResDto = ChequeResDto.builder()
                    .id(cardHistoryResDto.getId())
                    .transactionTime(cardHistoryResDto.getDate())
                    .message("Sent successfully")
                    .build();

            // If cheque is needed, populate cheque details
            if (needCheque) {
                // Mask card details
                cardHistoryService.maskCard(cardResSender);
                cardHistoryService.maskCard(cardResReceiver);
                chequeResDto.setChequeIsNeed(true);
                chequeResDto.setAmount(String.valueOf(transformBalance));
                chequeResDto.setCommission(String.valueOf(transformBalance * 0.01));
                // Set sender and receiver card details
                chequeResDto.setCardHolderSenderDto(cardResSender.getCardHolder());
                chequeResDto.setCardHolderReceiverDto(cardResReceiver.getCardHolder());
                chequeResDto.setSenderCardNumber(cardResSender.getCardNumber());
                chequeResDto.setReceiverCardNumber(cardResReceiver.getCardNumber());
            }
            return chequeResDto;
        } catch (Exception ex) {
            // In case of an error, throw an exception with a database error message
            throw new DatabaseException(ex.getMessage());
        }
    }


    /**
     * Retrieves the count of banknote nominals required for cashing based on the cashing request and card information.
     *
     * @param cashingReqDto The DTO containing the cashing request details.
     * @param card          The card for which cashing is requested.
     * @return              A map representing the count of banknote nominals required for cashing.
     * @throws DatabaseException if an error occurs while interacting with the database.
     */
    private Map<Integer, Integer> getNominalsCount(CashingReqDto cashingReqDto, Card card) {
        try {
            amountCollected = 0L;
            List<Integer> bankNotesList = new ArrayList<>();
            List<Integer> quantitiesList = new ArrayList<>();
            // Retrieve cashing type information
            String cashingTypeId = cashingReqDto.getCashingTypeId();
            String cashingAmount = cashingReqDto.getAmount();
            Optional<CashingType> byId = cashingTypeRepository.findById(Long.valueOf(cashingTypeId));
            // Check if cashing type exists
            if (byId.isEmpty()) {
                throw new NotFoundException("Cashing type not found, with id: " + cashingTypeId);
            }
            CashingType cashingType = byId.get();
            CurrencyType currencyType = card.getCardType().getCurrencyType();
            // Retrieve banknote types for the specified cashing type and currency type
            List<BanknoteType> banknoteTypes = bankNoteRepository.findAllByCashingTypeId(cashingType);
            // Check if banknote types exist
            if (banknoteTypes.isEmpty()) {
                throw new NotFoundException("No banknotes found");
            }
            // Filter banknote types by currency type and sort them
            banknoteTypes = banknoteTypes.stream()
                    .filter(b -> b.getCurrencyTypeId().equals(currencyType))
                    .sorted()
                    .toList();
            // Populate lists of banknote nominals and quantities
            banknoteTypes.forEach(bankNote -> {
                bankNotesList.add(bankNote.getNominal());
                quantitiesList.add(bankNote.getQuantity());
            });
            // Calculate the count of banknote nominals required for cashing
            return getCashingNominalsByType(bankNotesList, quantitiesList, Long.parseLong(cashingAmount));
        } catch (Exception ex) {
            throw new DatabaseException("CashingService: getNominalsCount: " + ex.getMessage());
        }
    }



    /**
     * Determines the count of banknote nominals required for cashing based on the available banknote quantities.
     *
     * @param nominals             The list of available banknote nominals.
     * @param quantities           The list of available banknote quantities.
     * @param requiredCashingAmount The amount to be cashed.
     * @return                     A map representing the count of banknote nominals required for cashing.
     */
    private Map<Integer, Integer> getCashingNominalsByType(List<Integer> nominals,
                                                           List<Integer> quantities,
                                                           Long requiredCashingAmount) {
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


    /**
     * Converts a history card into a cashing response DTO.
     *
     * @param historyCard    The history card object representing the cashing transaction.
     * @param isChequeNeed   A boolean indicating whether a cheque is needed for the cashing transaction.
     * @param nominalsCount  A map representing the count of banknote nominals required for cashing.
     * @return               A ChequeResDto object representing the cashing response.
     */
    private ChequeResDto toCashingResDto(HistoryCard historyCard, boolean isChequeNeed, Map<Integer, Integer> nominalsCount) {
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
