package com.github.lajospolya.popularVote.entity.geo

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("municipality")
data class Municipality(
    @Id
    val id: Int? = null,
    val name: String,
    val provinceTerritoryId: Int,
)
