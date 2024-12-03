package com.github.lajospolya.popularVote.dto

data class VoteDto(
    val citizenId: Long,
    val policyId: Long,
    val selectionId: Long,
)
