package com.github.lajospolya.popularVote.dto

import com.github.lajospolya.popularVote.entity.PoliticalAffiliation

data class DeclarePoliticianDto(
    val levelOfPoliticsId: Int,
    val geographicLocation: String?,
    val politicalAffiliation: PoliticalAffiliation,
)
