package com.github.lajospolya.popularVote.entity

import org.springframework.data.annotation.Id

data class Opinion(
    @Id
    val id: Long,
    val politicalAffiliation: PoliticalAffiliation,
    val description: String,
    val author: String,
    val policyId: Long,
)
