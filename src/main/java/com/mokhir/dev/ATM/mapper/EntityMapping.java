package com.mokhir.dev.ATM.mapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import java.util.List;
import java.util.Set;

public interface EntityMapping<E, RQ, RS> {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    E toEntity(RQ req);

    RS toDto(E entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(RQ req, @MappingTarget E entity);

    List<E> toEntity(List<RQ> dtoList);

    List<RS> toDto(List<E> entityList);

    Set<E> toEntity(Set<RQ> rqSet);

    Set<RS> toDto(Set<E> eSet);

}

