package com.github.lajospolya.popularVote.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("political_party")
data class PoliticalParty(
    @Id
    val id: Int? = null,
    val displayName: String,
    val hexColor: String,
    val description: String?,
)
