package com.mokhir.dev.ATM.service;

import com.google.gson.Gson;
import com.mokhir.dev.ATM.aggregate.dto.req_dto.CardTypeReqDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.CardTypeResDto;
import com.mokhir.dev.ATM.aggregate.entity.CardType;
import com.mokhir.dev.ATM.aggregate.entity.CurrencyType;
import com.mokhir.dev.ATM.exceptions.DatabaseException;
import com.mokhir.dev.ATM.exceptions.NotFoundException;
import com.mokhir.dev.ATM.mapper.CardTypeMapper;
import com.mokhir.dev.ATM.repository.CardTypeRepository;
import com.mokhir.dev.ATM.repository.CurrencyTypeRepository;
import com.mokhir.dev.ATM.service.interfacies.CardTypeInterface;
import com.mokhir.dev.ATM.service.network.NetworkDataService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CardTypeService implements CardTypeInterface<CardTypeReqDto, CardTypeResDto> {
    private final Gson gson;
    private final CardTypeMapper cardTypeMapper;
    private final NetworkDataService networkDataService;
    private final CardTypeRepository cardTypeRepository;
    private final CurrencyTypeRepository currencyTypeRepository;
    private static final Logger LOG = LoggerFactory.getLogger(CardService.class);


    @Override
    public CardTypeResDto create(CardTypeReqDto cardTypeReqDto, HttpServletRequest servletRequest) {
       try {
           String ClientInfo = networkDataService.getClientIPv4Address(servletRequest);
           String ClientIP = networkDataService.getRemoteUserInfo(servletRequest);
           LOG.info("Client host : \t\t {}", gson.toJson(ClientInfo));
           LOG.info("Client IP :  \t\t {}", gson.toJson(ClientIP));

           CardType entity = cardTypeMapper.toEntity(cardTypeReqDto);
           Long id = cardTypeReqDto.getCurrencyTypeId();
           Optional<CurrencyType> byId = currencyTypeRepository.findById(id);
           if (byId.isEmpty()){
               throw new NotFoundException("Currency type not found, with id:%d".formatted(id));
           }
           CurrencyType currencyType = byId.get();
           entity.setCurrencyType(currencyType);
           entity.setName(entity.getName().toUpperCase());
           CardType save = cardTypeRepository.save(entity);
           return cardTypeMapper.toDto(save);
       }catch (DataIntegrityViolationException ex){
           throw new DataIntegrityViolationException(ex.getMessage(), ex.getCause());
       }
       catch (Exception ex) {
           LOG.error("CardTypeService: create: {}", ex.getMessage());
           throw new DatabaseException("CardTypeService: create: " + ex.getMessage());
       }
    }
}
