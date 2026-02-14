package com.github.lajospolya.popularVote.mapper

import com.github.lajospolya.popularVote.dto.CitizenDto
import com.github.lajospolya.popularVote.dto.CitizenProfileDto
import com.github.lajospolya.popularVote.dto.CitizenSelfDto
import com.github.lajospolya.popularVote.dto.CreateCitizenDto
import com.github.lajospolya.popularVote.entity.Citizen
import com.github.lajospolya.popularVote.entity.PoliticalAffiliation
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants
import org.mapstruct.Named

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
abstract class CitizenMapper {
    @Mapping(target = "politicalAffiliation", source = "politicalPartyId")
    abstract fun toDto(citizen: Citizen): CitizenDto

    @Mapping(target = "politicalAffiliation", source = "citizen.politicalPartyId")
    @Mapping(target = "policyCount", source = "policyCount")
    @Mapping(target = "voteCount", source = "voteCount")
    @Mapping(target = "levelOfPoliticsId", source = "levelOfPoliticsId")
    abstract fun toProfileDto(
        citizen: Citizen,
        policyCount: Long,
        voteCount: Long,
        levelOfPoliticsId: Int?,
    ): CitizenProfileDto

    @Mapping(target = "politicalAffiliation", source = "citizen.politicalPartyId")
    @Mapping(target = "policyCount", source = "policyCount")
    @Mapping(target = "voteCount", source = "voteCount")
    @Mapping(target = "isVerificationPending", source = "isVerificationPending")
    abstract fun toSelfDto(
        citizen: Citizen,
        policyCount: Long,
        voteCount: Long,
        isVerificationPending: Boolean,
    ): CitizenSelfDto

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authId", source = "authId")
    @Mapping(target = "citizenPoliticalDetailsId", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "politicalPartyId", source = "citizenDto.politicalAffiliation")
    abstract fun toEntity(
        citizenDto: CreateCitizenDto,
        authId: String,
    ): Citizen

    fun idToAffiliation(id: Int): PoliticalAffiliation = PoliticalAffiliation.fromId(id)

    fun affiliationToId(affiliation: PoliticalAffiliation): Int = affiliation.id
}
