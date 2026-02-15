package com.github.lajospolya.popularVote.dto

import com.github.lajospolya.popularVote.entity.PoliticalAffiliation

data class CreateCitizenDto(
    val givenName: String,
    val surname: String,
    val middleName: String?,
    val politicalAffiliation: PoliticalAffiliation,
)
