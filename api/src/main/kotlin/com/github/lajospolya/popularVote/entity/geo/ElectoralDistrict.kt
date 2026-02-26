package com.github.lajospolya.popularVote.entity.geo

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("electoral_district")
data class ElectoralDistrict(
    @Id
    val id: Int? = null,
    val name: String,
    val provinceTerritoryId: Int,
    val levelOfPoliticsId: Int,
)
