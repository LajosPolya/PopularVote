package com.github.lajospolya.PopularVote.dto

import com.github.lajospolya.PopularVote.entity.PoliticalSpectrum

data class CreateOpinionDto(
    val politicalSpectrum: PoliticalSpectrum,
    val description: String,
    val author: String,
    val policyId: Long,
)
