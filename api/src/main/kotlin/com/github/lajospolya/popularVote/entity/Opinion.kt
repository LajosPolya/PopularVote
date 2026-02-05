package com.github.lajospolya.popularVote.entity

import org.springframework.data.annotation.Id

data class Opinion(
    @Id
    val id: Long? = null,
    val description: String,
    val authorId: Long,
    val policyId: Long,
)
