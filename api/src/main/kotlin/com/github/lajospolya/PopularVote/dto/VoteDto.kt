package com.github.lajospolya.PopularVote.dto

data class VoteDto(
    val citizenId: Long,
    val policyId: Long,
    val selectionId: Long
)