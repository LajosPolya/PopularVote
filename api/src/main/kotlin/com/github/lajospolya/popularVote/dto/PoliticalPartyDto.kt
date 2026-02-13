package com.github.lajospolya.popularVote.dto

data class PoliticalPartyDto(
    val id: Int,
    val displayName: String,
    val hexColor: String,
    val description: String?,
)
