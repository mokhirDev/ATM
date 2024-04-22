package com.mokhir.dev.ATM.mapper;

import com.mokhir.dev.ATM.aggregate.dto.req_dto.CardHolderReqDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.CardHolderResDto;
import com.mokhir.dev.ATM.aggregate.entity.CardHolder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CardHolderMapper extends EntityMapping<CardHolder, CardHolderReqDto, CardHolderResDto>{
}
