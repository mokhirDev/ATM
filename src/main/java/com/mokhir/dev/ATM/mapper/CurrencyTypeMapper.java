package com.mokhir.dev.ATM.mapper;

import com.mokhir.dev.ATM.aggregate.dto.req_dto.CurrencyTypeReqDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.CurrencyTypeResDto;
import com.mokhir.dev.ATM.aggregate.entity.CurrencyType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CurrencyTypeMapper extends EntityMapping<CurrencyType, CurrencyTypeReqDto, CurrencyTypeResDto> {
}
