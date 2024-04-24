package com.mokhir.dev.ATM.service;

import com.google.gson.Gson;
import com.mokhir.dev.ATM.aggregate.dto.req_dto.CurrencyTypeReqDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.CurrencyTypeResDto;
import com.mokhir.dev.ATM.aggregate.entity.CurrencyType;
import com.mokhir.dev.ATM.exceptions.DatabaseException;
import com.mokhir.dev.ATM.exceptions.NotFoundException;
import com.mokhir.dev.ATM.mapper.CurrencyTypeMapper;
import com.mokhir.dev.ATM.repository.CurrencyTypeRepository;
import com.mokhir.dev.ATM.service.interfacies.CurrencyTypeInterface;
import com.mokhir.dev.ATM.service.network.NetworkDataService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CurrencyTypeService implements CurrencyTypeInterface<CurrencyTypeReqDto, CurrencyTypeResDto> {
    private final Gson gson;
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
            LOG.info("Successfully created currency type: {}", save);
            return currencyTypeMapper.toDto(save);
        } catch (DataIntegrityViolationException ex) {
            throw new DataIntegrityViolationException(ex.getMessage(), ex.getCause());
        } catch (ConstraintViolationException ex) {
            throw new ConstraintViolationException(ex.getConstraintViolations());
        } catch (Exception ex) {
            throw new DatabaseException("CardTypeService: create: " + ex.getMessage());
        }
    }

    public Page<CurrencyTypeResDto> findAll(Pageable pageable, HttpServletRequest servletRequest) {
        try {
            String ClientInfo = networkDataService.getClientIPv4Address(servletRequest);
            String ClientIP = networkDataService.getRemoteUserInfo(servletRequest);
            LOG.info("Client host : \t\t {}", gson.toJson(ClientInfo));
            LOG.info("Client IP :  \t\t {}", gson.toJson(ClientIP));

            Page<CurrencyType> all = currencyTypeRepository.findAll(pageable);
            Page<CurrencyTypeResDto> map = all.map(currencyTypeMapper::toDto);
            LOG.info("CardTypeService: findAll: {}", map);
            return map;
        } catch (Exception ex) {
            throw new DatabaseException("CardTypeService: findAll: " + ex.getMessage());
        }
    }

    public CurrencyTypeResDto deleteById(Long id, HttpServletRequest servletRequest) {
        try {
            String ClientInfo = networkDataService.getClientIPv4Address(servletRequest);
            String ClientIP = networkDataService.getRemoteUserInfo(servletRequest);
            LOG.info("Client host : \t\t {}", gson.toJson(ClientInfo));
            LOG.info("Client IP :  \t\t {}", gson.toJson(ClientIP));

            Optional<CurrencyType> byId = currencyTypeRepository.findById(id);
            if (byId.isEmpty()) {
                throw new NotFoundException("Currency with id:%d did not found".formatted(id));
            }
            CurrencyType currencyType = byId.get();
            currencyTypeRepository.deleteById(id);
            LOG.info("Delete successfully: {}", currencyType);
            return currencyTypeMapper.toDto(currencyType);
        } catch (Exception ex) {
            throw new DatabaseException("CardTypeService: deleteById: " + ex.getMessage());
        }
    }

    public CurrencyTypeResDto updateById(CurrencyTypeReqDto reqDto, HttpServletRequest servletRequest) {
        try {
            String ClientInfo = networkDataService.getClientIPv4Address(servletRequest);
            String ClientIP = networkDataService.getRemoteUserInfo(servletRequest);
            LOG.info("Client host : \t\t {}", gson.toJson(ClientInfo));
            LOG.info("Client IP :  \t\t {}", gson.toJson(ClientIP));

            Optional<CurrencyType> byId = currencyTypeRepository.findById(reqDto.getId());
            if (byId.isEmpty()) {
                throw new NotFoundException("Currency with id:%d did not found".formatted(reqDto.getId()));
            }
            CurrencyType currencyType = byId.get();
            currencyTypeMapper.updateFromDto(reqDto, currencyType);
            currencyType.setName(currencyType.getName().toUpperCase());
            currencyTypeRepository.save(currencyType);
            LOG.info("Updated successfully: {}", currencyType);
            return currencyTypeMapper.toDto(currencyType);
        } catch (Exception ex) {
            throw new DatabaseException("CardTypeService: updateById: " + ex.getMessage());
        }
    }
}
