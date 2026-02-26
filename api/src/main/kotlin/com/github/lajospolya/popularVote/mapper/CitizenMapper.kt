package com.github.lajospolya.popularVote.mapper

import com.github.lajospolya.popularVote.dto.CitizenDto
import com.github.lajospolya.popularVote.dto.CitizenProfileDto
import com.github.lajospolya.popularVote.dto.CitizenSelfDto
import com.github.lajospolya.popularVote.dto.CreateCitizenDto
import com.github.lajospolya.popularVote.dto.geo.PostalCodeDto
import com.github.lajospolya.popularVote.entity.Citizen
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
abstract class CitizenMapper {
    @Mapping(target = "politicalAffiliationId", source = "politicalPartyId")
    @Mapping(target = "postalCode", source = "postalCode")
    @Mapping(target = "id", source = "citizen.id")
    abstract fun toDto(
        citizen: Citizen,
        politicalPartyId: Int?,
        postalCode: PostalCodeDto? = null,
    ): CitizenDto

    @Mapping(target = "politicalAffiliationId", source = "politicalPartyId")
    @Mapping(target = "policyCount", source = "policyCount")
    @Mapping(target = "voteCount", source = "voteCount")
    @Mapping(target = "postalCode", source = "postalCode")
    @Mapping(target = "id", source = "citizen.id")
    abstract fun toProfileDto(
        citizen: Citizen,
        policyCount: Long,
        voteCount: Long,
        levelOfPoliticsName: String?,
        electoralDistrictName: String?,
        politicalPartyId: Int?,
        postalCode: PostalCodeDto? = null,
    ): CitizenProfileDto

    @Mapping(target = "politicalAffiliationId", source = "politicalPartyId")
    @Mapping(target = "policyCount", source = "policyCount")
    @Mapping(target = "voteCount", source = "voteCount")
    @Mapping(target = "isVerificationPending", source = "isVerificationPending")
    @Mapping(target = "postalCode", source = "postalCode")
    @Mapping(target = "id", source = "citizen.id")
    abstract fun toSelfDto(
        citizen: Citizen,
        policyCount: Long,
        voteCount: Long,
        isVerificationPending: Boolean,
        politicalPartyId: Int?,
        postalCode: PostalCodeDto? = null,
    ): CitizenSelfDto

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authId", source = "authId")
    @Mapping(target = "role", ignore = true)
    abstract fun toEntity(
        citizenDto: CreateCitizenDto,
        authId: String,
    ): Citizen
}
