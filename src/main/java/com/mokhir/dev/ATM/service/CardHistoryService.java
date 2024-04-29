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
import com.mokhir.dev.ATM.repository.CardHistoryRepository;
import com.mokhir.dev.ATM.repository.CardRepository;
import com.mokhir.dev.ATM.service.interfacies.CardHistoryServiceInterface;
import com.mokhir.dev.ATM.service.network.NetworkDataService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    /**
     * Retrieves the card history based on the provided criteria.
     *
     * @param cardHistoryReqDto The request DTO containing filtering parameters.
     * @param servletRequest    The HTTP servlet request (used for client information).
     * @param pageable          The pagination information.
     * @return A page of {@link CardHistoryResDto} representing the filtered card history.
     * @throws DatabaseException if the requested card is not found.
     */
    @Override
    @Transactional
    public Page<CardHistoryResDto> getCardHistory(CardHistoryReqDto cardHistoryReqDto,
                                                  HttpServletRequest servletRequest, Pageable pageable) {
        try {
            // Get client information
            String clientInfo = networkDataService.getClientIPv4Address(servletRequest);
            String clientIP = networkDataService.getRemoteUserInfo(servletRequest);
            LOG.info("Client host: {}", gson.toJson(clientInfo));
            LOG.info("Client IP: {}", gson.toJson(clientIP));

            // Retrieve card by ID
            Long cardId = cardHistoryReqDto.getIdFrom();
            Optional<Card> byId = cardRepository.findById(cardId);
            if (byId.isEmpty()) {
                throw new DatabaseException("Card not found, with id: %d".formatted(cardId));
            }
            Card card = byId.get();

            // Fetch card history
            Page<HistoryCard> allByFromCardOrToCard = cardHistoryRepository
                    .findAllByFromCardOrToCard(card, card, pageable);

            // Create an anonymous representation of card history
            return doAnonymousCardHistoryRes(allByFromCardOrToCard);
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }


    /**
     * Records a cash withdrawal transaction.
     *
     * @param amount The amount being withdrawn.
     * @return The transaction history as a {@link HistoryCard}.
     * @throws DatabaseException if there's an issue with saving the transaction.
     */
    public HistoryCard cashing(Long amount) {
        try {
            BigDecimal amountCashing = BigDecimal.valueOf(amount);
            BigDecimal cashingCommission = amountCashing.multiply(BigDecimal.valueOf(0.01));

            // Create a new history entry
            HistoryCard historyCard = HistoryCard.builder()
                    .amount(amountCashing)
                    .date(LocalDateTime.now())
                    .commission(cashingCommission)
                    .build();

            // Save the history entry
            cardHistoryRepository.save(historyCard);

            return historyCard;
        } catch (Exception ex) {
            throw new DatabaseException(ex.getMessage());
        }
    }


    /**
     * Creates an anonymous representation of card history by masking sensitive information.
     *
     * @param historyCards The original card history page.
     * @return A new page of {@link CardHistoryResDto} with masked information.
     */
    private Page<CardHistoryResDto> doAnonymousCardHistoryRes(Page<HistoryCard> historyCards) {
        historyCards.forEach(cardHistory -> {
            if (cardHistory.getFromCard() != null) {
                CardHolderResDto cardHolderFromRes = cardHolderMapper.toDto(cardHistory
                        .getFromCard()
                        .getCardHolder());
                CardResDto fromCardRes = cardMapper.toDto(cardHistory.getFromCard());
                fromCardRes.setCardHolder(cardHolderFromRes);
            }
            if (cardHistory.getToCard() != null) {
                CardHolderResDto cardHolderToRes = cardHolderMapper.toDto(cardHistory
                        .getToCard()
                        .getCardHolder());
                CardResDto toCardRes = cardMapper.toDto(cardHistory.getToCard());
                toCardRes.setCardHolder(cardHolderToRes);
            }
        });

        Page<CardHistoryResDto> cardHistoryResDtoList = historyCards
                .map(cardHistoryMapper::toDto);

        cardHistoryResDtoList.forEach(this::maskingCardHistory);
        return cardHistoryResDtoList;
    }


    /**
     * Masks sensitive information in a {@link CardHistoryResDto} for privacy.
     *
     * @param cardHistoryResDto The card history response DTO to be masked.
     */
    public void maskingCardHistory(CardHistoryResDto cardHistoryResDto) {
        // Mask sender card details
        CardResDto senderCard = cardHistoryResDto.getFromCard();
        cardHistoryResDto.setFromCard(maskCard(senderCard));

        // Mask receiver card details
        CardResDto receiverCard = cardHistoryResDto.getToCard();
        cardHistoryResDto.setToCard(maskCard(receiverCard));
    }


    /**
     * Masks sensitive information in a {@link CardResDto} for privacy.
     *
     * @param cardResDto The card response DTO to be masked.
     * @return A new {@link CardResDto} with masked information.
     */
    public CardResDto maskCard(CardResDto cardResDto) {
        if (cardResDto == null) {
            return null;
        }

        // Mask card number
        String cardNumber = cardResDto.getCardNumber();
        String maskedCardNumber = cardNumber.substring(0, 6) + "******" + cardNumber.substring(12);
        cardResDto.setCardNumber(maskedCardNumber);

        // Mask cardholder's phone number
        CardHolderResDto cardHolder = cardResDto.getCardHolder();
        String phoneNumber = cardHolder.getPhoneNumber();
        String maskedPhoneNumber = phoneNumber.substring(0, 4) + "****" + phoneNumber.substring(8);
        cardHolder.setPhoneNumber(maskedPhoneNumber);

        // Mask cardholder's name
        String name = cardHolder.getName();
        String maskedName = maskString(name);
        cardHolder.setName(maskedName);

        // Mask cardholder's last name
        String lastName = cardHolder.getLastName();
        String maskedLastName = maskString(lastName);
        cardHolder.setLastName(maskedLastName);

        return cardResDto;
    }



    /**
     * Masks a string by replacing all characters except the first and last with asterisks.
     *
     * @param str The input string to be masked.
     * @return The masked string.
     */
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



    /**
     * Records a cash deposit transaction for the specified card.
     *
     * @param fillingCard     The card receiving the cash deposit.
     * @param amount          The amount being deposited.
     * @param servletRequest  The HTTP servlet request (used for client information).
     * @return The transaction history as a {@link HistoryCard}.
     * @throws DatabaseException if the requested card is not found.
     */
    public HistoryCard fill(Card fillingCard, long amount,
                            HttpServletRequest servletRequest) {
        try {
            // Get client information
            String clientInfo = networkDataService.getClientIPv4Address(servletRequest);
            String clientIP = networkDataService.getRemoteUserInfo(servletRequest);
            LOG.info("Client host: {}", gson.toJson(clientInfo));
            LOG.info("Client IP: {}", gson.toJson(clientIP));

            BigDecimal amountCashing = BigDecimal.valueOf(amount);
            BigDecimal cashingCommission = amountCashing.multiply(BigDecimal.valueOf(0.01));

            // Create a new history entry
            HistoryCard historyCard = HistoryCard.builder()
                    .amount(amountCashing)
                    .date(LocalDateTime.now())
                    .toCard(fillingCard)
                    .commission(cashingCommission)
                    .build();

            // Save the history entry
            cardHistoryRepository.save(historyCard);

            return historyCard;
        } catch (Exception ex) {
            throw new DatabaseException(ex.getMessage());
        }
    }


    /**
     * Transforms the balance between two cards, recording the transaction history.
     *
     * @param sender          The card from which the balance is transferred.
     * @param receiver        The card receiving the transformed balance.
     * @param transformBalance The amount to be transformed.
     * @return The transaction history as a {@link HistoryCard}.
     */
    public HistoryCard transform(Card sender, Card receiver, long transformBalance) {
        BigDecimal amountCashing = BigDecimal.valueOf(transformBalance);
        BigDecimal cashingCommission = amountCashing.multiply(BigDecimal.valueOf(0.01));

        // Create a new history entry
        HistoryCard historyCard = HistoryCard.builder()
                .amount(amountCashing)
                .date(LocalDateTime.now())
                .fromCard(sender)
                .toCard(receiver)
                .commission(cashingCommission)
                .build();

        // Save the history entry
        cardHistoryRepository.save(historyCard);

        return historyCard;
    }


    /**
     * Retrieves a page of cashed card history based on the provided criteria.
     *
     * @param cardHistoryReqDto The request DTO containing filtering parameters.
     * @param servletRequest    The HTTP servlet request (used for client information).
     * @param pageable          The pagination information.
     * @return A page of {@link CardHistoryResDto} representing the filtered cashed card history.
     * @throws DatabaseException if the requested card is not found.
     */
    public Page<CardHistoryResDto> getCardCashedHistory(
            CardHistoryReqDto cardHistoryReqDto,
            HttpServletRequest servletRequest,
            Pageable pageable) {
        try {
            // Get client information
            String clientInfo = networkDataService.getClientIPv4Address(servletRequest);
            String clientIP = networkDataService.getRemoteUserInfo(servletRequest);
            LOG.info("Client host: {}", gson.toJson(clientInfo));
            LOG.info("Client IP: {}", gson.toJson(clientIP));

            // Retrieve card by ID
            Long cardId = cardHistoryReqDto.getIdFrom();
            Optional<Card> byId = cardRepository.findById(cardId);
            if (byId.isEmpty()) {
                throw new DatabaseException("Card not found, with id: %d".formatted(cardId));
            }
            Card card = byId.get();

            // Fetch cashed card history
            Page<HistoryCard> allByFromCardOrToCard = cardHistoryRepository
                    .findAllByFromCardOrToCard(card, card, pageable);

            // Filter history to include only cashed cards
            List<HistoryCard> filteredHistoryCardList = allByFromCardOrToCard
                    .stream()
                    .filter(cardHistory ->
                            cardHistory.getFromCard() != null && cardHistory.getToCard() == null)
                    .collect(Collectors.toList());

            // Create a new page with filtered results
            Page<HistoryCard> filteredHistoryCardPage = new PageImpl<>(
                    filteredHistoryCardList, pageable, filteredHistoryCardList.size());

            // Convert to response DTO
            return doAnonymousCardHistoryRes(filteredHistoryCardPage);
        } catch (Exception ex) {
            throw new DatabaseException(ex.getMessage());
        }
    }


    /**
     * Retrieves a page of transferred card history based on the provided criteria.
     *
     * @param cardHistoryReqDto The request DTO containing filtering parameters.
     * @param servletRequest    The HTTP servlet request (used for client information).
     * @param pageable          The pagination information.
     * @return A page of {@link CardHistoryResDto} representing the filtered transferred card history.
     * @throws DatabaseException if the requested card is not found.
     */
    public Page<CardHistoryResDto> getCardTransferredHistory(
            CardHistoryReqDto cardHistoryReqDto,
            HttpServletRequest servletRequest,
            Pageable pageable) {
        try {
            // Get client information
            String clientInfo = networkDataService.getClientIPv4Address(servletRequest);
            String clientIP = networkDataService.getRemoteUserInfo(servletRequest);
            LOG.info("Client host: {}", gson.toJson(clientInfo));
            LOG.info("Client IP: {}", gson.toJson(clientIP));

            // Retrieve card by ID
            Long cardId = cardHistoryReqDto.getIdFrom();
            Optional<Card> byId = cardRepository.findById(cardId);
            if (byId.isEmpty()) {
                throw new DatabaseException("Card not found, with id: %d".formatted(cardId));
            }
            Card card = byId.get();

            // Fetch transferred card history
            Page<HistoryCard> allByFromCardOrToCard = cardHistoryRepository
                    .findAllByFromCardOrToCard(card, card, pageable);

            // Filter history to include only transferred cards from this specific card
            List<HistoryCard> filteredHistoryCardList = allByFromCardOrToCard
                    .stream()
                    .filter(cardHistory ->
                            cardHistory.getFromCard() != null && cardHistory.getToCard() != null)
                    .filter(cardHistory ->
                            cardHistory.getFromCard().getCardNumber().equals(card.getCardNumber()))
                    .collect(Collectors.toList());

            // Create a new page with filtered results
            Page<HistoryCard> filteredHistoryCardPage = new PageImpl<>(
                    filteredHistoryCardList, pageable, filteredHistoryCardList.size());

            // Convert to response DTO
            return doAnonymousCardHistoryRes(filteredHistoryCardPage);
        } catch (Exception ex) {
            throw new DatabaseException(ex.getMessage());
        }
    }


    /**
     * Retrieves a page of filled card history based on the provided criteria.
     *
     * @param cardHistoryReqDto The request DTO containing filtering parameters.
     * @param servletRequest    The HTTP servlet request (used for client information).
     * @param pageable          The pagination information.
     * @return A page of {@link CardHistoryResDto} representing the filtered card history.
     * @throws DatabaseException if the requested card is not found.
     */
    public Page<CardHistoryResDto> getCardFilledHistory(
            CardHistoryReqDto cardHistoryReqDto,
            HttpServletRequest servletRequest,
            Pageable pageable) {
        try {
            // Get client information
            String clientInfo = networkDataService.getClientIPv4Address(servletRequest);
            String clientIP = networkDataService.getRemoteUserInfo(servletRequest);
            LOG.info("Client host: {}", gson.toJson(clientInfo));
            LOG.info("Client IP: {}", gson.toJson(clientIP));

            // Retrieve card by ID
            Long cardId = cardHistoryReqDto.getIdFrom();
            Optional<Card> byId = cardRepository.findById(cardId);
            if (byId.isEmpty()) {
                throw new DatabaseException("Card not found, with id: %d".formatted(cardId));
            }
            Card card = byId.get();

            // Fetch card history
            Page<HistoryCard> allByFromCardOrToCard = cardHistoryRepository
                    .findAllByFromCardOrToCard(card, card, pageable);

            // Filter history to include only filled cards
            List<HistoryCard> filteredHistoryCardList = allByFromCardOrToCard
                    .stream()
                    .filter(cardHistory ->
                            cardHistory.getFromCard() == null && cardHistory.getToCard() != null)
                    .collect(Collectors.toList());

            // Create a new page with filtered results
            Page<HistoryCard> filteredHistoryCardPage = new PageImpl<>(
                    filteredHistoryCardList, pageable, filteredHistoryCardList.size());

            // Convert to response DTO
            return doAnonymousCardHistoryRes(filteredHistoryCardPage);
        } catch (Exception ex) {
            throw new DatabaseException(ex.getMessage());
        }
    }



    /**
     * Retrieves card history for a specific date.
     *
     * @param cardHistoryReqDto The DTO containing the card ID and date for which to retrieve history.
     * @param servletRequest    The servlet request object to extract client information.
     * @param pageable          The pageable object to specify pagination parameters.
     * @return                  A page of card history DTOs for the specified date.
     * @throws DatabaseException if an error occurs while interacting with the database.
     */
    public Page<CardHistoryResDto> getCardDateHistory(CardHistoryReqDto cardHistoryReqDto,
                                                      HttpServletRequest servletRequest, Pageable pageable) {
        try {
            // Retrieve client information from the servlet request
            String clientInfo = networkDataService.getClientIPv4Address(servletRequest);
            String clientIP = networkDataService.getRemoteUserInfo(servletRequest);
            // Log client information
            LOG.info("Client host: {}", gson.toJson(clientInfo));
            LOG.info("Client IP: {}", gson.toJson(clientIP));

            // Retrieve card ID from DTO
            Long cardId = cardHistoryReqDto.getIdFrom();
            // Find the card by ID
            Optional<Card> byId = cardRepository.findById(cardId);
            if (byId.isEmpty()) {
                // Throw exception if card not found
                throw new DatabaseException("Card not found, with id: %d".formatted(cardId));
            }

            // Retrieve date from DTO
            LocalDateTime date = cardHistoryReqDto.getDate();
            // Get the card entity from optional
            Card card = byId.get();
            // Retrieve card history from repository for the specified date
            Page<HistoryCard> allByFromCardOrToCard = cardHistoryRepository.findAllByDateLike(date, pageable);
            // Convert card history to a list
            List<HistoryCard> filteredHistoryCardList = allByFromCardOrToCard
                    .stream()
                    .collect(Collectors.toList());

            // Create a page of card history DTOs
            Page<HistoryCard> filteredHistoryCardPage = new PageImpl<>(filteredHistoryCardList, pageable, filteredHistoryCardList.size());
            return doAnonymousCardHistoryRes(filteredHistoryCardPage);
        } catch (Exception ex) {
            // In case of an error, throw an exception with a database error message
            throw new DatabaseException(ex.getMessage());
        }
    }
}
