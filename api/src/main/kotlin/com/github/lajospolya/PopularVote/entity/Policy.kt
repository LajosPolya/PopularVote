package com.github.lajospolya.PopularVote.entity

import org.springframework.data.annotation.Id

data class Policy(
    @Id
    val id: Long,
    val description: String,
)
