package com.mokhir.dev.ATM.mapper;

import com.mokhir.dev.ATM.aggregate.dto.req_dto.BankNoteReqDto;
import com.mokhir.dev.ATM.aggregate.dto.res_dto.BankNoteResDto;
import com.mokhir.dev.ATM.aggregate.entity.BanknoteType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BankNoteMapper extends EntityMapping<BanknoteType, BankNoteReqDto, BankNoteResDto>{
}
