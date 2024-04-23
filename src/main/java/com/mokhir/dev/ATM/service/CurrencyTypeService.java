package com.mokhir.dev.ATM.service;

import com.google.gson.Gson;
import com.mokhir.dev.ATM.aggregate.dto.req_dto.CurrencyTypeReqDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.CurrencyTypeResDto;
import com.mokhir.dev.ATM.aggregate.entity.CurrencyType;
import com.mokhir.dev.ATM.exceptions.DatabaseException;
import com.mokhir.dev.ATM.mapper.CurrencyTypeMapper;
import com.mokhir.dev.ATM.repository.CurrencyTypeRepository;
import com.mokhir.dev.ATM.service.interfacies.CurrencyTypeInterface;
import com.mokhir.dev.ATM.service.network.NetworkDataService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrencyTypeService implements CurrencyTypeInterface<CurrencyTypeReqDto, CurrencyTypeResDto> {
    private final Gson gson;
    private final CurrencyTypeRepository repository;
    private final NetworkDataService networkDataService;
    private final CurrencyTypeRepository currencyTypeRepository;
    private final CurrencyTypeMapper currencyTypeMapper;
    private static final Logger LOG = LoggerFactory.getLogger(CardService.class);


    @Override
    public CurrencyTypeResDto create(CurrencyTypeReqDto currencyTypeReqDto, HttpServletRequest servletRequest) {
        try {
            String ClientInfo = networkDataService.getClientIPv4Address(servletRequest);
            String ClientIP = networkDataService.getRemoteUserInfo(servletRequest);
            LOG.info("Client host : \t\t {}", gson.toJson(ClientInfo));
            LOG.info("Client IP :  \t\t {}", gson.toJson(ClientIP));

            CurrencyType entity = currencyTypeMapper.toEntity(currencyTypeReqDto);
            entity.setName(entity.getName().toUpperCase());
            CurrencyType save = currencyTypeRepository.save(entity);
            return currencyTypeMapper.toDto(save);
        } catch (Exception ex) {
            LOG.error("CardTypeService: create: {}", ex.getMessage());
            throw new DatabaseException("CardTypeService: create: " + ex.getMessage());
        }
    }
}
