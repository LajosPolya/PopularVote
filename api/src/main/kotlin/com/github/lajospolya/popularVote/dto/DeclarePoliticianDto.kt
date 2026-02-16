package com.github.lajospolya.popularVote.dto

data class DeclarePoliticianDto(
    val levelOfPoliticsId: Int,
    val geographicLocation: String?,
    val politicalAffiliationId: Int,
)
