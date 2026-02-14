package com.github.lajospolya.popularVote.mapper

import com.github.lajospolya.popularVote.dto.CitizenPoliticalDetailsDto
import com.github.lajospolya.popularVote.entity.CitizenPoliticalDetails
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface CitizenPoliticalDetailsMapper {
    fun toDto(citizenPoliticalDetails: CitizenPoliticalDetails): CitizenPoliticalDetailsDto

    @Mapping(target = "id", ignore = true)
    fun toEntity(citizenPoliticalDetailsDto: CitizenPoliticalDetailsDto): CitizenPoliticalDetails
}
