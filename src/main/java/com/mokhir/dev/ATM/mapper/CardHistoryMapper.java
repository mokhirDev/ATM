package com.mokhir.dev.ATM.mapper;

import com.mokhir.dev.ATM.aggregate.dto.req_dto.CardHistoryReqDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.CardHistoryResDto;
import com.mokhir.dev.ATM.aggregate.entity.HistoryCard;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CardHistoryMapper extends EntityMapping<HistoryCard, CardHistoryReqDto, CardHistoryResDto>{
}
