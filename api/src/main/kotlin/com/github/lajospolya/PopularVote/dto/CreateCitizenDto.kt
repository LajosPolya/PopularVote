package com.github.lajospolya.PopularVote.dto

data class CreateCitizenDto (
    val givenName: String,
    val surname: String,
    val middleName: String?,
)