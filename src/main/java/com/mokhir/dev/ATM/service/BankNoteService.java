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

    /**
     * Creates a new banknote entry based on the provided DTO.
     *
     * @param bankNoteReqDto  The DTO containing the information for the new banknote.
     * @param servletRequest  The servlet request object to extract client information.
     * @return                The DTO representing the newly created banknote.
     * @throws DatabaseException if an error occurs while interacting with the database.
     */
    @Override
    public BankNoteResDto create(BankNoteReqDto bankNoteReqDto, HttpServletRequest servletRequest) {
        try {
            // Retrieve client information from the servlet request
            String clientInfo = networkDataService.getClientIPv4Address(servletRequest);
            String clientIP = networkDataService.getRemoteUserInfo(servletRequest);
            // Log client information
            LOG.info("Client host: {}", gson.toJson(clientInfo));
            LOG.info("Client IP: {}", gson.toJson(clientIP));

            // Convert DTO to entity
            BanknoteType entity = bankNoteMapper.toEntity(bankNoteReqDto);

            // Retrieve cashing type from repository
            Optional<CashingType> cashingTypeById = cashingTypeRepository.findById(bankNoteReqDto.getCashingType());
            if (cashingTypeById.isEmpty()) {
                // Throw exception if cashing type not found
                throw new NotFoundException("Cashing type not found, with id: " + bankNoteReqDto.getCashingType());
            }

            // Retrieve currency type from repository
            Optional<CurrencyType> currencyTypeById = currencyTypeRepository.findById(bankNoteReqDto.getCurrencyType());
            if (currencyTypeById.isEmpty()) {
                // Throw exception if currency type not found
                throw new NotEnoughFundsException("Currency type not found, with id: " + bankNoteReqDto.getCurrencyType());
            }

            // Set cashing type and currency type to the banknote entity
            CashingType cashingType = cashingTypeById.get();
            CurrencyType currencyType = currencyTypeById.get();
            entity.setCashingTypeId(cashingType);
            entity.setCurrencyTypeId(currencyType);

            // Save the banknote entity to the repository
            bankNoteRepository.save(entity);

            // Convert the entity back to DTO and return
            return bankNoteMapper.toDto(entity);
        } catch (Exception ex) {
            // In case of an error, throw an exception with a database error message
            throw new DatabaseException(ex.getMessage());
        }
    }


    /**
     * Excludes the necessary denominations of banknotes from the storage.
     *
     * @param nominalsCount A map of denominations and their quantities to exclude.
     *                      Key - banknote denomination, value - number of banknotes of that denomination.
     * @throws DatabaseException if an error occurs while interacting with the database.
     */
    public void excludeNecessaryNominals(Map<Integer, Integer> nominalsCount) {
        try {
            // Iterate over each denomination and its quantity
            nominalsCount.forEach((nominal, quantity) -> {
                if (quantity > 0) {
                    // Find the banknote by denomination
                    BanknoteType byNominal = bankNoteRepository.findByNominal(nominal);
                    Integer nominalQuantity = byNominal.getQuantity();
                    // Decrease the quantity of banknotes of the required denomination
                    byNominal.setQuantity(nominalQuantity - quantity);
                    // Save the changes in the repository
                    bankNoteRepository.save(byNominal);
                }
            });
            // Log the successful exclusion of necessary denominations
            LOG.info("Successfully Excluded necessary nominals: \t\t {}", gson.toJson(nominalsCount));
        } catch (Exception ex) {
            // In case of an error, throw an exception with a database error message
            throw new DatabaseException(ex.getMessage());
        }
    }
}
