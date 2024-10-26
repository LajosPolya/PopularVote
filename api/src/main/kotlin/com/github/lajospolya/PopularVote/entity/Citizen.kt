package com.github.lajospolya.PopularVote.entity

import org.springframework.data.annotation.Id

data class Citizen(
    @Id
    val id: Long,
    val givenName: String,
    val surname: String,
    val middleName: String?,
)
