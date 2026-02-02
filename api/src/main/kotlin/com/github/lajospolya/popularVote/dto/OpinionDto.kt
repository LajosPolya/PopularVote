package com.github.lajospolya.popularVote.dto

import com.github.lajospolya.popularVote.entity.PoliticalAffiliation

data class OpinionDto(
    val id: Long,
    val politicalAffiliation: PoliticalAffiliation,
    val description: String,
    val author: String,
    val policyId: Long,
)
