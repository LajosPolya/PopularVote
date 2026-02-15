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

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
abstract class CitizenMapper {
    abstract fun toDto(citizen: Citizen): CitizenDto

    @Mapping(target = "politicalAffiliation", source = "politicalPartyId")
    @Mapping(target = "policyCount", source = "policyCount")
    @Mapping(target = "voteCount", source = "voteCount")
    abstract fun toProfileDto(
        citizen: Citizen,
        politicalPartyId: Int,
        policyCount: Long,
        voteCount: Long,
        levelOfPoliticsName: String?,
    ): CitizenProfileDto

    @Mapping(target = "politicalAffiliation", source = "politicalPartyId")
    @Mapping(target = "policyCount", source = "policyCount")
    @Mapping(target = "voteCount", source = "voteCount")
    @Mapping(target = "isVerificationPending", source = "isVerificationPending")
    abstract fun toSelfDto(
        citizen: Citizen,
        politicalPartyId: Int,
        policyCount: Long,
        voteCount: Long,
        isVerificationPending: Boolean,
    ): CitizenSelfDto

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authId", source = "authId")
    @Mapping(target = "role", ignore = true)
    abstract fun toEntity(
        citizenDto: CreateCitizenDto,
        authId: String,
    ): Citizen

    fun idToAffiliation(id: Int): PoliticalAffiliation = PoliticalAffiliation.fromId(id)

    fun affiliationToId(affiliation: PoliticalAffiliation): Int = affiliation.id
}
