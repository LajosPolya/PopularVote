package com.github.lajospolya.popularVote.dto

data class CreatePoliticalPartyDto(
    val displayName: String,
    val hexColor: String,
    val description: String?,
    val levelOfPoliticsId: Long,
    val provinceAndTerritoryId: Int?,
)
