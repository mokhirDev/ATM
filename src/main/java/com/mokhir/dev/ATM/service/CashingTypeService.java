package com.mokhir.dev.ATM.service;

import com.google.gson.Gson;
import com.mokhir.dev.ATM.aggregate.dto.req_dto.CashingTypeReqDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.CashingTypeResDto;
import com.mokhir.dev.ATM.aggregate.entity.CashingType;
import com.mokhir.dev.ATM.exceptions.DatabaseException;
import com.mokhir.dev.ATM.mapper.CashingTypeMapper;
import com.mokhir.dev.ATM.repository.CashingTypeRepository;
import com.mokhir.dev.ATM.service.interfacies.CashingTypeServiceInterface;
import com.mokhir.dev.ATM.service.network.NetworkDataService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CashingTypeService implements CashingTypeServiceInterface<CashingTypeReqDto, CashingTypeResDto> {
    private final Gson gson;
    private final NetworkDataService networkDataService;
    private final CashingTypeRepository cashingTypeRepository;
    private final CashingTypeMapper cashingTypeMapper;
    private static final Logger LOG = LoggerFactory.getLogger(CardService.class);

    /**
     * Creates a new cashing type based on the provided request DTO.
     *
     * @param cashingTypeReqDto The DTO containing the details of the cashing type to be created.
     * @param servletRequest    The HTTP servlet request.
     * @return                  A CashingTypeResDto object representing the newly created cashing type.
     * @throws DatabaseException if an error occurs while interacting with the database.
     */
    @Override
    public CashingTypeResDto create(CashingTypeReqDto cashingTypeReqDto, HttpServletRequest servletRequest) {
        try {
            String ClientInfo = networkDataService.getClientIPv4Address(servletRequest);
            String ClientIP = networkDataService.getRemoteUserInfo(servletRequest);
            LOG.info("Client host : \t\t {}", gson.toJson(ClientInfo));
            LOG.info("Client IP :  \t\t {}", gson.toJson(ClientIP));

            CashingType entity = cashingTypeMapper.toEntity(cashingTypeReqDto);
            cashingTypeRepository.save(entity);
            return cashingTypeMapper.toDto(entity);
        } catch (Exception ex) {
            throw new DatabaseException(ex.getMessage());
        }
    }


}
