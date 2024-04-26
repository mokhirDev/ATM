package com.mokhir.dev.ATM.mapper;

import com.mokhir.dev.ATM.aggregate.dto.req_dto.CashingTypeReqDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.CashingTypeResDto;
import com.mokhir.dev.ATM.aggregate.entity.CashingType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CashingTypeMapper extends EntityMapping<CashingType, CashingTypeReqDto, CashingTypeResDto> {
}
