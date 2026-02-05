package com.github.lajospolya.popularVote.dto

data class CreateOpinionDto(
    val description: String,
    val policyId: Long,
)
