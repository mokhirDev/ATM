package com.mokhir.dev.ATM.aggregate.dto.res_dto;

import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseMessage<T> {
    private String message;
    private T entities;
}
