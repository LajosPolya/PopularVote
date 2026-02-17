package com.github.lajospolya.popularVote.dto

import com.github.lajospolya.popularVote.entity.Role

import com.github.lajospolya.popularVote.dto.geo.PostalCodeDto

data class CitizenDto(
    val id: Long,
    val givenName: String,
    val surname: String,
    val middleName: String?,
    val politicalAffiliationId: Int?,
    val role: Role,
    val postalCodeId: Int?,
    val postalCode: PostalCodeDto?,
) {
    val fullName: String
        get() = "$givenName $surname"
}
