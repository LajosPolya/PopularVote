package com.github.lajospolya.PopularVote.dto

data class CitizenDto(
    val id: Long,
    val givenName: String,
    val surname: String,
    val middleName: String?,
)
