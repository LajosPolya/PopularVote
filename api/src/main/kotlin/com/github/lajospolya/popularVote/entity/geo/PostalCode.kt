package com.github.lajospolya.popularVote.entity.geo

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("postal_code")
data class PostalCode(
    @Id
    val id: Int? = null,
    val name: String,
    val code: Int,
    val municipalityId: Int,
    val federalElectoralDistrictId: Int,
)
