package com.github.lajospolya.popularVote.dto

import com.github.lajospolya.popularVote.entity.PoliticalAffiliation

data class OpinionDetailsDto(
    val id: Long,
    val description: String,
    val authorId: Long,
    val authorName: String,
    val policyId: Long,
)

data class PolicyDetailsDto(
    val id: Long,
    val description: String,
    val publisherCitizenId: Long,
    val publisherName: String,
    val publisherPoliticalAffiliation: PoliticalAffiliation,
    val opinions: List<OpinionDetailsDto>,
)
