package com.github.lajospolya.PopularVote.mapper

import com.github.lajospolya.PopularVote.dto.CitizenDto
import com.github.lajospolya.PopularVote.entity.Citizen
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface CitizenMapper {

    fun entityToDto(citizen: Citizen): CitizenDto

    fun toEntity(citizenDto: CitizenDto): Citizen
}