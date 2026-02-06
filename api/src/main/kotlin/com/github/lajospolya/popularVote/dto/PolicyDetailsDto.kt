package com.github.lajospolya.popularVote.dto

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
    val opinions: List<OpinionDetailsDto>,
)
