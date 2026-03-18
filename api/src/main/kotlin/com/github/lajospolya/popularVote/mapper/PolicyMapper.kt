package com.github.lajospolya.popularVote.mapper

import com.github.lajospolya.popularVote.dto.CitizenDto
import com.github.lajospolya.popularVote.dto.CreatePolicyDto
import com.github.lajospolya.popularVote.dto.OpinionDetailsDto
import com.github.lajospolya.popularVote.dto.PolicyDetailsDto
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
    @Mapping(target = "creationDate", source = "policy.creationDate")
    @Mapping(target = "provinceAndTerritoryId", source = "policy.provinceAndTerritoryId")
    fun toDto(
        policy: Policy,
        coAuthorCitizens: List<CitizenDto>,
    ): PolicyDto

    @Mapping(target = "publisherName", source = "publisherName")
    @Mapping(target = "isBookmarked", source = "isBookmarked")
    @Mapping(target = "levelOfPoliticsId", source = "policy.levelOfPoliticsId")
    @Mapping(target = "publisherPoliticalPartyId", source = "publisherPoliticalPartyId")
    @Mapping(target = "provinceAndTerritoryId", source = "policy.provinceAndTerritoryId")
    fun toSummaryDto(
        policy: Policy,
        publisherName: String,
        isBookmarked: Boolean,
        publisherPoliticalPartyId: Int?,
    ): PolicySummaryDto

    @Mapping(target = "id", source = "policy.id")
    @Mapping(target = "description", source = "policy.description")
    @Mapping(target = "publisherCitizenId", source = "policy.publisherCitizenId")
    @Mapping(target = "levelOfPoliticsId", source = "policy.levelOfPoliticsId")
    @Mapping(target = "publisherName", source = "publisherName")
    @Mapping(target = "publisherPoliticalAffiliationId", source = "publisherPoliticalAffiliationId")
    @Mapping(target = "coAuthorCitizens", source = "coAuthorCitizens")
    @Mapping(target = "opinions", source = "opinions")
    @Mapping(target = "closeDate", source = "policy.closeDate")
    @Mapping(target = "creationDate", source = "policy.creationDate")
    @Mapping(target = "provinceAndTerritoryId", source = "policy.provinceAndTerritoryId")
    fun toDetailsDto(
        policy: Policy,
        publisherName: String,
        publisherPoliticalAffiliationId: Int?,
        coAuthorCitizens: List<CitizenDto>,
        opinions: List<OpinionDetailsDto>,
    ): PolicyDetailsDto

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publisherCitizenId", source = "publisherCitizenId")
    @Mapping(target = "levelOfPoliticsId", source = "levelOfPoliticsId")
    @Mapping(target = "provinceAndTerritoryId", source = "provinceAndTerritoryId")
    @Mapping(target = "creationDate", source = "policy.creationDate", defaultExpression = "java(java.time.LocalDateTime.now())")
    fun toEntity(
        policy: CreatePolicyDto,
        publisherCitizenId: Long,
        levelOfPoliticsId: Int,
        provinceAndTerritoryId: Int?,
    ): Policy
}
