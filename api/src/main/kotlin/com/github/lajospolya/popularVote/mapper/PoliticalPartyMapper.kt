package com.github.lajospolya.popularVote.mapper

import com.github.lajospolya.popularVote.dto.CreatePoliticalPartyDto
import com.github.lajospolya.popularVote.dto.PoliticalPartyDto
import com.github.lajospolya.popularVote.entity.PoliticalParty
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface PoliticalPartyMapper {
    fun toDto(politicalParty: PoliticalParty): PoliticalPartyDto

    @Mapping(target = "id", ignore = true)
    fun toEntity(createPoliticalPartyDto: CreatePoliticalPartyDto): PoliticalParty
}
