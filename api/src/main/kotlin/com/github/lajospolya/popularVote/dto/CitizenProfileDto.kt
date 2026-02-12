package com.github.lajospolya.popularVote.dto

import com.github.lajospolya.popularVote.entity.PoliticalAffiliation
import com.github.lajospolya.popularVote.entity.Role

data class CitizenProfileDto(
    val id: Long,
    val givenName: String,
    val surname: String,
    val middleName: String?,
    val politicalAffiliation: PoliticalAffiliation,
    val role: Role,
    val policyCount: Long,
    val voteCount: Long,
)
