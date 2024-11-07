package com.github.lajospolya.popularVote.entity

import org.springframework.data.annotation.Id

data class Policy(
    @Id
    val id: Long,
    val description: String,
)
