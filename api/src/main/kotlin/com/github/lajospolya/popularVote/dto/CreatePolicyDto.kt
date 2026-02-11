package com.github.lajospolya.popularVote.dto

data class CreatePolicyDto(
    val description: String,
    val coAuthorCitizenIds: List<Long>,
)
