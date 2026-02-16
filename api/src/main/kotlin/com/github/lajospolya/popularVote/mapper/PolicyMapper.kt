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
    @Mapping(target = "levelOfPoliticsId", source = "policy.levelOfPoliticsId")
    @Mapping(target = "citizenPoliticalDetailsId", source = "policy.citizenPoliticalDetailsId")
    @Mapping(target = "creationDate", source = "policy.creationDate")
    fun toDto(
        policy: Policy,
        coAuthorCitizens: List<CitizenDto>,
    ): PolicyDto

    @Mapping(target = "publisherName", source = "publisherName")
    @Mapping(target = "isBookmarked", source = "isBookmarked")
    @Mapping(target = "levelOfPoliticsId", source = "policy.levelOfPoliticsId")
    @Mapping(target = "citizenPoliticalDetailsId", source = "policy.citizenPoliticalDetailsId")
    @Mapping(target = "publisherPoliticalPartyId", source = "publisherPoliticalPartyId")
    fun toSummaryDto(
        policy: Policy,
        publisherName: String,
        isBookmarked: Boolean,
        publisherPoliticalPartyId: Int?,
    ): PolicySummaryDto

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publisherCitizenId", source = "publisherCitizenId")
    @Mapping(target = "levelOfPoliticsId", source = "levelOfPoliticsId")
    @Mapping(target = "citizenPoliticalDetailsId", source = "citizenPoliticalDetailsId")
    @Mapping(target = "creationDate", expression = "java(LocalDateTime.now())")
    fun toEntity(
        policy: CreatePolicyDto,
        publisherCitizenId: Long,
        levelOfPoliticsId: Int,
        citizenPoliticalDetailsId: Long,
    ): Policy
}
