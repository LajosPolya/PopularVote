package com.github.lajospolya.popularVote.mapper

import com.github.lajospolya.popularVote.dto.CitizenDto
import com.github.lajospolya.popularVote.dto.CreatePolicyDto
import com.github.lajospolya.popularVote.dto.PolicyDto
import com.github.lajospolya.popularVote.dto.PolicySummaryDto
import com.github.lajospolya.popularVote.entity.Policy
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = [CitizenMapper::class])
interface PolicyMapper {
    @Mapping(target = "coAuthorCitizens", source = "coAuthorCitizens")
    fun toDto(
        policy: Policy,
        coAuthorCitizens: List<CitizenDto>,
    ): PolicyDto

    @Mapping(target = "publisherName", source = "publisherName")
    fun toSummaryDto(
        policy: Policy,
        publisherName: String,
    ): PolicySummaryDto

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publisherCitizenId", source = "publisherCitizenId")
    fun toEntity(
        policy: CreatePolicyDto,
        publisherCitizenId: Long,
    ): Policy
}
