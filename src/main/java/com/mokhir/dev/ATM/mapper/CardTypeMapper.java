package com.mokhir.dev.ATM.mapper;

import com.mokhir.dev.ATM.aggregate.dto.req_dto.CardTypeReqDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.CardTypeResDto;
import com.mokhir.dev.ATM.aggregate.entity.CardType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CardTypeMapper extends EntityMapping<CardType, CardTypeReqDto, CardTypeResDto>{
}
