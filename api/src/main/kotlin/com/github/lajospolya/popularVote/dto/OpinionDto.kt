package com.github.lajospolya.popularVote.dto

data class OpinionDto(
    val id: Long,
    val description: String,
    val author: String,
    val policyId: Long,
)
