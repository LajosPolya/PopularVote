package com.github.lajospolya.popularVote.dto

data class CitizenDto(
    val id: Long,
    val givenName: String,
    val surname: String,
    val middleName: String?,
)
