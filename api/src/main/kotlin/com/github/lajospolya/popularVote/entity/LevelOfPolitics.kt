package com.github.lajospolya.popularVote.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("level_of_politics")
data class LevelOfPolitics(
    @Id
    val id: Int? = null,
    val name: String,
    val description: String?,
)
