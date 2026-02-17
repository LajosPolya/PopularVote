package com.github.lajospolya.popularVote.entity.geo

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("province_and_territory")
data class ProvinceAndTerritory(
    @Id
    val id: Int? = null,
    val name: String,
)
