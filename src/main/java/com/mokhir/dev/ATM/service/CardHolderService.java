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

    @Override
    public CardHolderResDto createCardHolder(CardHolderReqDto cardHolderReqDto, HttpServletRequest servletRequest) {
        try {
            String ClientInfo = networkDataService.getClientIPv4Address(servletRequest);
            String ClientIP = networkDataService.getRemoteUserInfo(servletRequest);
            LOG.info("Client host : \t\t {}", gson.toJson(ClientInfo));
            LOG.info("Client IP :  \t\t {}", gson.toJson(ClientIP));

            CardHolder entity = cardHolderMapper.toEntity(cardHolderReqDto);
            entity.setPassportSeries(entity.getPassportSeries().toUpperCase());
            cardHolderRepository.save(entity);
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

    public CardHolderResDto updateCardHolder(CardHolderReqDto updateDto, HttpServletRequest servletRequest) {
        try {
            String ClientInfo = networkDataService.getClientIPv4Address(servletRequest);
            String ClientIP = networkDataService.getRemoteUserInfo(servletRequest);
            LOG.info("Client host : \t\t {}", gson.toJson(ClientInfo));
            LOG.info("Client IP :  \t\t {}", gson.toJson(ClientIP));

            Optional<CardHolder> byId = cardHolderRepository.findById(updateDto.getId());
            if (byId.isEmpty()) {
                throw new NotFoundException("User not found with id:%d".formatted(updateDto.getId()));
            }
            CardHolder cardHolder = byId.get();
            cardHolderMapper.updateFromDto(updateDto, cardHolder);
            cardHolderRepository.save(cardHolder);
            CardHolderResDto dto = cardHolderMapper.toDto(cardHolder);
            LOG.info("CardHolder updated successfully: {}", dto.toString());
            return dto;
        } catch (DataIntegrityViolationException ex) {
            throw new DataIntegrityViolationException(ex.getMessage(), ex.getCause());
        } catch (ConstraintViolationException ex) {
            throw new ConstraintViolationException(ex.getConstraintViolations());
        } catch (Exception ex) {
            throw new DatabaseException("CardHolderService: createCardHolder: " + ex.getMessage());
        }
    }
}
