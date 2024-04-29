package com.mokhir.dev.ATM.service;

import com.google.gson.Gson;
import com.mokhir.dev.ATM.aggregate.dto.req_dto.CardHolderReqDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.CardHolderResDto;
import com.mokhir.dev.ATM.aggregate.entity.CardHolder;
import com.mokhir.dev.ATM.exceptions.DatabaseException;
import com.mokhir.dev.ATM.exceptions.NotFoundException;
import com.mokhir.dev.ATM.mapper.CardHolderMapper;
import com.mokhir.dev.ATM.repository.CardHolderRepository;
import com.mokhir.dev.ATM.service.interfacies.CardHolderServiceInterface;
import com.mokhir.dev.ATM.service.network.NetworkDataService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CardHolderService implements CardHolderServiceInterface<CardHolderReqDto, CardHolderResDto> {
    private final Gson gson;
    private final CardHolderMapper cardHolderMapper;
    private final CardHolderRepository cardHolderRepository;
    private final NetworkDataService networkDataService;
    private static final Logger LOG = LoggerFactory.getLogger(CardService.class);

    /**
     * Creates a new card holder based on the provided information.
     *
     * @param cardHolderReqDto The request DTO containing card holder details.
     * @param servletRequest   The HTTP servlet request (used for client information).
     * @return The created card holder as a {@link CardHolderResDto}.
     * @throws DataIntegrityViolationException if there's a data integrity violation during creation.
     * @throws ConstraintViolationException    if there's a constraint violation during creation.
     * @throws DatabaseException               if there's an issue with saving the card holder.
     */
    @Override
    public CardHolderResDto createCardHolder(CardHolderReqDto cardHolderReqDto, HttpServletRequest servletRequest) {
        try {
            // Get client information
            String clientInfo = networkDataService.getClientIPv4Address(servletRequest);
            String clientIP = networkDataService.getRemoteUserInfo(servletRequest);
            LOG.info("Client host: {}", gson.toJson(clientInfo));
            LOG.info("Client IP: {}", gson.toJson(clientIP));

            // Convert request DTO to entity
            CardHolder entity = cardHolderMapper.toEntity(cardHolderReqDto);
            entity.setPassportSeries(entity.getPassportSeries().toUpperCase());

            // Save the card holder
            cardHolderRepository.save(entity);

            // Convert entity to response DTO
            CardHolderResDto dto = cardHolderMapper.toDto(entity);
            LOG.info("Cardholder successfully created: {}", dto);
            return dto;
        } catch (DataIntegrityViolationException ex) {
            throw new DataIntegrityViolationException(ex.getMessage(), ex.getCause());
        } catch (ConstraintViolationException ex) {
            throw new ConstraintViolationException(ex.getConstraintViolations());
        } catch (Exception ex) {
            throw new DatabaseException("CardHolderService: createCardHolder: " + ex.getMessage());
        }
    }


    /**
     * Updates an existing card holder based on the provided information.
     *
     * @param updateDto       The request DTO containing updated card holder details.
     * @param servletRequest  The HTTP servlet request (used for client information).
     * @return The updated card holder as a {@link CardHolderResDto}.
     * @throws NotFoundException if the requested card holder is not found.
     * @throws DataIntegrityViolationException if there's a data integrity violation during update.
     * @throws ConstraintViolationException if there's a constraint violation during update.
     * @throws DatabaseException if there's an issue with saving the updated card holder.
     */
    public CardHolderResDto updateCardHolder(CardHolderReqDto updateDto, HttpServletRequest servletRequest) {
        try {
            // Get client information
            String clientInfo = networkDataService.getClientIPv4Address(servletRequest);
            String clientIP = networkDataService.getRemoteUserInfo(servletRequest);
            LOG.info("Client host: {}", gson.toJson(clientInfo));
            LOG.info("Client IP: {}", gson.toJson(clientIP));

            // Retrieve card holder by ID
            Optional<CardHolder> byId = cardHolderRepository.findById(updateDto.getId());
            if (byId.isEmpty()) {
                throw new NotFoundException("User not found with id: %d".formatted(updateDto.getId()));
            }
            CardHolder cardHolder = byId.get();

            // Update card holder details
            cardHolderMapper.updateFromDto(updateDto, cardHolder);

            // Save the updated card holder
            cardHolderRepository.save(cardHolder);

            // Convert entity to response DTO
            CardHolderResDto dto = cardHolderMapper.toDto(cardHolder);
            LOG.info("CardHolder updated successfully: {}", dto.toString());
            return dto;
        } catch (DataIntegrityViolationException ex) {
            throw new DataIntegrityViolationException(ex.getMessage(), ex.getCause());
        } catch (ConstraintViolationException ex) {
            throw new ConstraintViolationException(ex.getConstraintViolations());
        } catch (Exception ex) {
            throw new DatabaseException("CardHolderService: updateCardHolder: " + ex.getMessage());
        }
    }

}
