package com.mokhir.dev.ATM.service;

import com.google.gson.Gson;
import com.mokhir.dev.ATM.aggregate.dto.req_dto.BankNoteReqDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.BankNoteResDto;
import com.mokhir.dev.ATM.aggregate.entity.BanknoteType;
import com.mokhir.dev.ATM.aggregate.entity.CashingType;
import com.mokhir.dev.ATM.aggregate.entity.CurrencyType;
import com.mokhir.dev.ATM.exceptions.DatabaseException;
import com.mokhir.dev.ATM.exceptions.NotEnoughFundsException;
import com.mokhir.dev.ATM.exceptions.NotFoundException;
import com.mokhir.dev.ATM.mapper.BankNoteMapper;
import com.mokhir.dev.ATM.repository.BankNoteRepository;
import com.mokhir.dev.ATM.repository.CashingTypeRepository;
import com.mokhir.dev.ATM.repository.CurrencyTypeRepository;
import com.mokhir.dev.ATM.service.interfacies.BankNoteInterface;
import com.mokhir.dev.ATM.service.network.NetworkDataService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BankNoteService implements BankNoteInterface<BankNoteReqDto, BankNoteResDto> {
    private final Gson gson;
    private final NetworkDataService networkDataService;
    private final BankNoteRepository bankNoteRepository;
    private final BankNoteMapper bankNoteMapper;
    private final CashingTypeRepository cashingTypeRepository;
    private final CurrencyTypeRepository currencyTypeRepository;
    private static final Logger LOG = LoggerFactory.getLogger(CardService.class);

    @Override
    public BankNoteResDto create(BankNoteReqDto bankNoteReqDto, HttpServletRequest servletRequest) {
        try {
            String ClientInfo = networkDataService.getClientIPv4Address(servletRequest);
            String ClientIP = networkDataService.getRemoteUserInfo(servletRequest);
            LOG.info("Client host : \t\t {}", gson.toJson(ClientInfo));
            LOG.info("Client IP :  \t\t {}", gson.toJson(ClientIP));
            BanknoteType entity = bankNoteMapper.toEntity(bankNoteReqDto);
            Optional<CashingType> cashingTypeById = cashingTypeRepository.findById(bankNoteReqDto.getCashingType());
            if (cashingTypeById.isEmpty()) {
                throw new NotFoundException("Cashing type not found, with id: "
                        + bankNoteReqDto.getCashingType());
            }
            Optional<CurrencyType> currencyTypeById = currencyTypeRepository.findById(bankNoteReqDto.getCurrencyType());
            if (currencyTypeById.isEmpty()) {
                throw new NotEnoughFundsException("Currency type not found, with id: "
                        + bankNoteReqDto.getCurrencyType());
            }

            CashingType cashingType = cashingTypeById.get();
            CurrencyType currencyType = currencyTypeById.get();
            entity.setCashingTypeId(cashingType);
            entity.setCurrencyTypeId(currencyType);
            bankNoteRepository.save(entity);
            return bankNoteMapper.toDto(entity);
        } catch (Exception ex) {
            throw new DatabaseException(ex.getMessage());
        }
    }

    public void excludeNecessaryNominals(Map<Integer, Integer> nominalsCount) {
        try {
            nominalsCount.forEach((nominal, quantity) -> {
                if (quantity > 0) {
                    BanknoteType byNominal = bankNoteRepository.findByNominal(nominal);
                    Integer nominalQuantity = byNominal.getQuantity();
                    byNominal.setQuantity(nominalQuantity - quantity);
                    bankNoteRepository.save(byNominal);
                }
            });
            LOG.info("Successfully Excluded necessary nominals: \t\t {}", gson.toJson(nominalsCount));
        } catch (Exception ex) {
            throw new DatabaseException(ex.getMessage());
        }
    }
}
