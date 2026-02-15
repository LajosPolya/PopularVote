package com.github.lajospolya.popularVote.dto

import com.github.lajospolya.popularVote.entity.Role

data class CitizenDto(
    val id: Long,
    val givenName: String,
    val surname: String,
    val middleName: String?,
    val politicalAffiliationId: Int?,
    val role: Role,
) {
    val fullName: String
        get() = "$givenName $surname"
}
