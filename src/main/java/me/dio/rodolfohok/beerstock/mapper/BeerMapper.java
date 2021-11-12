package me.dio.rodolfohok.beerstock.mapper;

import me.dio.rodolfohok.beerstock.dto.BeerDTO;
import me.dio.rodolfohok.beerstock.entity.Beer;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface BeerMapper {

    BeerMapper INSTANCE = Mappers.getMapper(BeerMapper.class);

    Beer toModel(BeerDTO beerDTO);

    BeerDTO toDTO(Beer beer);
}
