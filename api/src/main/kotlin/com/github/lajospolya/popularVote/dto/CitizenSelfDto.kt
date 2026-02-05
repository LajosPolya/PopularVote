package com.github.lajospolya.popularVote.dto

import com.github.lajospolya.popularVote.entity.PoliticalAffiliation

data class CitizenSelfDto(
    val givenName: String,
    val surname: String,
    val middleName: String?,
    val politicalAffiliation: PoliticalAffiliation,
)
