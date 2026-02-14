package com.github.lajospolya.popularVote.dto

data class PolicyDto(
    val id: Long,
    val description: String,
    val publisherCitizenId: Long,
    val levelOfPoliticsId: Int,
    val citizenPoliticalDetailsId: Long,
    val coAuthorCitizens: List<CitizenDto>,
)
