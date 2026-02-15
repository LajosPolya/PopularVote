package com.github.lajospolya.popularVote.dto

import java.time.LocalDateTime

data class CreatePolicyDto(
    val description: String,
    val coAuthorCitizenIds: List<Long>,
    val closeDate: LocalDateTime,
)
