package com.github.lajospolya.popularVote.dto

data class PolicySummaryDto(
    val id: Long,
    val description: String,
    val publisherCitizenId: Long,
    val publisherName: String,
)
