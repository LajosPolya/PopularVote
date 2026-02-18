package com.github.lajospolya.popularVote.dto

import java.time.LocalDateTime

data class PolicyDto(
    val id: Long,
    val description: String,
    val publisherCitizenId: Long,
    val levelOfPoliticsId: Int,
    val coAuthorCitizens: List<CitizenDto>,
    val closeDate: LocalDateTime,
    val creationDate: LocalDateTime,
)
