package com.github.lajospolya.popularVote.dto

import com.github.lajospolya.popularVote.entity.PoliticalAffiliation

data class OpinionDetailsDto(
    val id: Long,
    val description: String,
    val authorId: Long,
    val authorName: String,
    val authorPoliticalAffiliation: PoliticalAffiliation,
    val policyId: Long,
)

data class PolicyDetailsDto(
    val id: Long,
    val description: String,
    val publisherCitizenId: Long,
    val levelOfPoliticsId: Int,
    val citizenPoliticalDetailsId: Long,
    val publisherName: String,
    val publisherPoliticalAffiliation: PoliticalAffiliation,
    val coAuthorCitizens: List<CitizenDto>,
    val opinions: List<OpinionDetailsDto>,
)
