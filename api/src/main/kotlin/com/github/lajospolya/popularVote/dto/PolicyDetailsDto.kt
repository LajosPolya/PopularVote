package com.github.lajospolya.popularVote.dto

import java.time.LocalDateTime

data class OpinionDetailsDto(
    val id: Long,
    val description: String,
    val authorId: Long,
    val authorName: String,
    val authorPoliticalAffiliationId: Int?,
    val policyId: Long,
)

data class PolicyDetailsDto(
    val id: Long,
    val description: String,
    val publisherCitizenId: Long,
    val levelOfPoliticsId: Int,
    val citizenPoliticalDetailsId: Long,
    val publisherName: String,
    val publisherPoliticalAffiliationId: Int?,
    val coAuthorCitizens: List<CitizenDto>,
    val opinions: List<OpinionDetailsDto>,
    val closeDate: LocalDateTime,
)
