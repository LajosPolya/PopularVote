package com.github.lajospolya.popularVote.dto

import com.github.lajospolya.popularVote.dto.geo.PostalCodeDto
import com.github.lajospolya.popularVote.entity.Role

data class CitizenSelfDto(
    val id: Long,
    val givenName: String,
    val surname: String,
    val middleName: String?,
    val politicalAffiliationId: Int?,
    val role: Role,
    val policyCount: Long,
    val voteCount: Long,
    val isVerificationPending: Boolean,
    val postalCodeId: Int?,
    val postalCode: PostalCodeDto?,
)
