package com.github.lajospolya.popularVote.dto

data class OpinionDto(
    val id: Long,
    val description: String,
    val authorId: Long,
    val policyId: Long,
)
