package com.github.lajospolya.popularVote.mapper

import com.github.lajospolya.popularVote.dto.CitizenDto
import com.github.lajospolya.popularVote.dto.CreateCitizenDto
import com.github.lajospolya.popularVote.entity.Citizen
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface CitizenMapper {
    fun toDto(citizen: Citizen): CitizenDto

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authId", source = "authId")
    fun toEntity(citizenDto: CreateCitizenDto, authId: String): Citizen
}
