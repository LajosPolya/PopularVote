package com.github.lajospolya.popularVote.dto

data class PolicySummaryDto(
    val id: Long,
    val description: String,
    val publisherCitizenId: Long,
    val levelOfPoliticsId: Int,
    val citizenPoliticalDetailsId: Long,
    val publisherName: String,
    val isBookmarked: Boolean,
)
