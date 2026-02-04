package com.github.lajospolya.popularVote.dto

import com.github.lajospolya.popularVote.entity.PoliticalAffiliation

data class CitizenDto(
    val id: Long,
    val givenName: String,
    val surname: String,
    val middleName: String?,
    val politicalAffiliation: PoliticalAffiliation,
    val authId: String,
)
