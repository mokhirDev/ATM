package com.mokhir.dev.ATM.service;

import com.google.gson.Gson;
import com.mokhir.dev.ATM.aggregate.dto.req_dto.CardReqDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.CardHolderResDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.CardResDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.ResponseMessage;
import com.mokhir.dev.ATM.aggregate.entity.Card;
import com.mokhir.dev.ATM.aggregate.entity.CardHolder;
import com.mokhir.dev.ATM.aggregate.entity.CardType;
import com.mokhir.dev.ATM.exceptions.DatabaseException;
import com.mokhir.dev.ATM.exceptions.NotFoundException;
import com.mokhir.dev.ATM.mapper.CardHolderMapper;
import com.mokhir.dev.ATM.mapper.CardMapper;
import com.mokhir.dev.ATM.repository.CardHolderRepository;
import com.mokhir.dev.ATM.repository.CardRepository;
import com.mokhir.dev.ATM.repository.CardTypeRepository;
import com.mokhir.dev.ATM.service.interfacies.CardServiceInterface;
import com.mokhir.dev.ATM.service.network.NetworkDataService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
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
    private final CardHolderMapper cardHolderMapper;
    private final NetworkDataService networkDataService;
    private final CardTypeRepository cardTypeRepository;
    private final CardHolderRepository cardHolderRepository;
    private static final Logger LOG = LoggerFactory.getLogger(CardService.class);


    /**
     * Creates a new card based on the provided information.
     *
     * @param cardReqDto      The request DTO containing card details.
     * @param servletRequest  The HTTP servlet request (used for client information).
     * @return The created card as a {@link CardResDto}.
     * @throws NotFoundException if the requested card holder or card type is not found.
     * @throws DataIntegrityViolationException if there's a data integrity violation during creation.
     * @throws ConstraintViolationException if there's a constraint violation during creation.
     * @throws DatabaseException if there's an issue with saving the card.
     */
    @Override
    @Transactional
    public CardResDto createCard(CardReqDto cardReqDto, HttpServletRequest servletRequest) {
        try {
            // Get client information
            String clientInfo = networkDataService.getClientIPv4Address(servletRequest);
            String clientIP = networkDataService.getRemoteUserInfo(servletRequest);
            LOG.info("Client host: {}", gson.toJson(clientInfo));
            LOG.info("Client IP: {}", gson.toJson(clientIP));

            // Convert request DTO to entity
            Card entity = cardMapper.toEntity(cardReqDto);

            // Retrieve card holder by ID
            Long id = cardReqDto.getUserId();
            Optional<CardHolder> byId = cardHolderRepository.findById(id);
            if (byId.isEmpty()) {
                throw new NotFoundException("Card holder with id: %d not found".formatted(id));
            }
            CardHolder cardHolder = byId.get();

            // Retrieve card type by ID
            Long cardTypeId = cardReqDto.getCardTypeId();
            Optional<CardType> byType = cardTypeRepository.findById(cardTypeId);
            if (byType.isEmpty()) {
                throw new NotFoundException("Card type with id: %d not found".formatted(cardTypeId));
            }
            CardType cardType = byType.get();

            // Set card properties
            LocalDate currentDate = LocalDate.now();
            LocalDate expirationDate = currentDate.plusYears(cardType.getExpirationYear());
            entity.setCardHolder(cardHolder);
            entity.setCheckCardQuantity(3);
            entity.setBalance(0.0);
            entity.setIsActive(true);
            entity.setCardExpireDate(expirationDate);
            entity.setCardCvc(String.valueOf(ThreadLocalRandom.current().nextInt(111, 999)));
            entity.setCardNumber(generateCardNumber(cardType.getNumber()));
            entity.setCardType(cardType);

            // Save the card
            cardRepository.save(entity);

            // Convert entity to response DTO
            CardResDto dto = cardMapper.toDto(entity);
            LOG.info("Successfully created card: {}", dto);
            return dto;
        } catch (ConstraintViolationException ex) {
            throw new ConstraintViolationException(ex.getConstraintViolations());
        } catch (Exception ex) {
            throw new DatabaseException("CardService: createCard: " + ex.getMessage());
        }
    }


    /**
     * Generates a new card number based on the provided prefix.
     *
     * @param number The prefix for the card number.
     * @return A new card number.
     */
    private String generateCardNumber(String number) {
        long nextLong = new Random().nextLong();
        nextLong = Math.abs(nextLong);
        return number + String.valueOf(nextLong).substring(0, 10);
    }


    /**
     * Retrieves all cards associated with a specific Personal Number and Foreigner's License (PnFl) number.
     *
     * @param pageable       The pagination information.
     * @param pinFlNumber    The PnFl number to search for.
     * @param servletRequest The HTTP servlet request (used for client information).
     * @return A page of {@link CardResDto} representing the cards associated with the specified PnFl number.
     * @throws NotFoundException if the requested card holder is not found.
     * @throws DatabaseException if there's an issue with retrieving the cards.
     */
    @Override
    public Page<CardResDto> getAllByPnFl(Pageable pageable, String pinFlNumber, HttpServletRequest servletRequest) {
        try {
            // Get client information
            String clientInfo = networkDataService.getClientIPv4Address(servletRequest);
            String clientIP = networkDataService.getRemoteUserInfo(servletRequest);
            LOG.info("Client host: {}", gson.toJson(clientInfo));
            LOG.info("Client IP: {}", gson.toJson(clientIP));

            // Retrieve card holder by PnFl number
            CardHolder byPnFlNumber = cardHolderRepository.findByPinFl(pinFlNumber);
            if (byPnFlNumber == null) {
                throw new NotFoundException("Card holder with PnFl number: %s not found".formatted(pinFlNumber));
            }

            // Fetch cards associated with the card holder
            Page<Card> allCardsByCardHolder = cardRepository.findAllCardsByCardHolder(byPnFlNumber, pageable);

            // Convert entities to response DTOs
            Page<CardResDto> cardResDtoPage = allCardsByCardHolder.map(cardMapper::toDto);

            // Set the same card holder for all cards in the page
            CardHolderResDto cardHolderResDto = cardHolderMapper.toDto(byPnFlNumber);
            cardResDtoPage.forEach(cardResDto -> cardResDto.setCardHolder(cardHolderResDto));

            LOG.info("Successfully retrieved cards: {}", gson.toJson(cardResDtoPage));
            return cardResDtoPage;
        } catch (Exception ex) {
            throw new DatabaseException("CardService: getAllByPnFl: " + ex.getMessage());
        }
    }


    /**
     * Retrieves all cards based on the provided pagination information.
     *
     * @param pageable       The pagination information.
     * @param servletRequest The HTTP servlet request (used for client information).
     * @return A page of {@link CardResDto} representing all cards.
     * @throws DatabaseException if there's an issue with retrieving the cards.
     */
    public Page<CardResDto> getAll(Pageable pageable, HttpServletRequest servletRequest) {
        try {
            // Get client information
            String clientInfo = networkDataService.getClientIPv4Address(servletRequest);
            String clientIP = networkDataService.getRemoteUserInfo(servletRequest);
            LOG.info("Client host: {}", gson.toJson(clientInfo));
            LOG.info("Client IP: {}", gson.toJson(clientIP));

            // Fetch all cards
            Page<Card> allCardsByCardHolder = cardRepository.findAll(pageable);

            // Convert entities to response DTOs
            Page<CardResDto> cardResDtoPage = allCardsByCardHolder.map(cardMapper::toDto);

            // Set the same card holder for all cards in the page
            cardResDtoPage.stream().forEach(cardResDto -> {
                CardHolder cardHolder = cardHolderRepository.findById(cardResDto.getCardHolder().getId()).get();
                CardHolderResDto cardHolderResDto = cardHolderMapper.toDto(cardHolder);
                cardResDto.setCardHolder(cardHolderResDto);
            });

            LOG.info("Successfully retrieved cards: {}", gson.toJson(cardResDtoPage));
            return cardResDtoPage;
        } catch (Exception ex) {
            throw new DatabaseException("CardService: getAll: " + ex.getMessage());
        }
    }


    /**
     * Retrieves a card by its ID.
     *
     * @param cardId          The ID of the card to retrieve.
     * @param servletRequest  The HTTP servlet request (used for client information).
     * @return The card as a {@link CardResDto}.
     * @throws NotFoundException if the requested card is not found.
     * @throws DatabaseException if there's an issue with retrieving the card.
     */
    public CardResDto getById(Long cardId, HttpServletRequest servletRequest) {
        try {
            // Get client information
            String clientInfo = networkDataService.getClientIPv4Address(servletRequest);
            String clientIP = networkDataService.getRemoteUserInfo(servletRequest);
            LOG.info("Client host: {}", gson.toJson(clientInfo));
            LOG.info("Client IP: {}", gson.toJson(clientIP));

            // Retrieve card by ID
            Optional<Card> byId = cardRepository.findById(cardId);
            if (byId.isEmpty()) {
                throw new NotFoundException("Card not found, with id: %d".formatted(cardId));
            }
            Card foundCard = byId.get();

            // Convert entity to response DTO
            CardResDto cardResDto = cardMapper.toDto(foundCard);

            // Set the same card holder for the card
            CardHolder foundCardHolder = foundCard.getCardHolder();
            CardHolderResDto cardHolderResDto = cardHolderMapper.toDto(foundCardHolder);
            cardResDto.setCardHolder(cardHolderResDto);

            return cardResDto;
        } catch (Exception ex) {
            throw new DatabaseException("CardService: getById: " + ex.getMessage());
        }
    }


    /**
     * Updates the PIN of a card based on the provided information.
     *
     * @param cardReqDto      The request DTO containing updated card details.
     * @param servletRequest  The HTTP servlet request (used for client information).
     * @return A response message indicating the success of the update.
     * @throws NotFoundException if the requested card is not found.
     * @throws DatabaseException if there's an issue with updating the card.
     */
    public ResponseMessage<CardResDto> updatePin(CardReqDto cardReqDto, HttpServletRequest servletRequest) {
        try {
            // Get client information
            String clientInfo = networkDataService.getClientIPv4Address(servletRequest);
            String clientIP = networkDataService.getRemoteUserInfo(servletRequest);
            LOG.info("Client host: {}", gson.toJson(clientInfo));
            LOG.info("Client IP: {}", gson.toJson(clientIP));

            // Retrieve card by ID
            Optional<Card> byId = cardRepository.findById(cardReqDto.getId());
            if (byId.isEmpty()) {
                throw new NotFoundException("Card not found, with id: %d".formatted(cardReqDto.getId()));
            }
            Card foundCard = byId.get();

            // Update the card PIN
            foundCard.setCardPin(Integer.valueOf(cardReqDto.getCardPin()));
            cardRepository.save(foundCard);

            // Create a response message
            ResponseMessage<CardResDto> responseMessage = new ResponseMessage<>();
            CardResDto dto = cardMapper.toDto(foundCard);
            dto.setCardHolder(cardHolderMapper.toDto(foundCard.getCardHolder()));
            responseMessage.setEntities(dto);
            responseMessage.setMessage("Password updated successfully");

            return responseMessage;
        } catch (Exception ex) {
            throw new DatabaseException("CardService: updatePin: " + ex.getMessage());
        }
    }

}
