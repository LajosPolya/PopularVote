package com.github.lajospolya.popularVote.dto

import java.time.LocalDateTime

data class CreatePolicyDto(
    val title: String,
    val description: String,
    val coAuthorCitizenIds: List<Long>,
    val closeDate: LocalDateTime,
    val creationDate: LocalDateTime? = null,
)
