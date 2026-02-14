package com.github.lajospolya.popularVote.entity

import org.springframework.data.annotation.Id

data class Policy(
    @Id
    val id: Long? = null,
    val description: String,
    val publisherCitizenId: Long,
    val levelOfPoliticsId: Int,
    val citizenPoliticalDetailsId: Long,
)
