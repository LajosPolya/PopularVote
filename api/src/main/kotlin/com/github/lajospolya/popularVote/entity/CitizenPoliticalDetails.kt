package com.github.lajospolya.popularVote.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("citizen_political_details")
data class CitizenPoliticalDetails(
    @Id
    val id: Long? = null,
    val citizenId: Long,
    val levelOfPoliticsId: Int,
    val geographicLocation: String?,
    val politicalPartyId: Int,
)
