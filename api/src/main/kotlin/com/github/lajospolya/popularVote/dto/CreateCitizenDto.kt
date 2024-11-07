package com.github.lajospolya.popularVote.dto

data class CreateCitizenDto (
    val givenName: String,
    val surname: String,
    val middleName: String?,
)