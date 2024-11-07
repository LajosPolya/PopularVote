package com.github.lajospolya.popularVote.dto

import com.github.lajospolya.popularVote.entity.PoliticalSpectrum

data class OpinionDto(
    val id: Long,
    val politicalSpectrum: PoliticalSpectrum,
    val description: String,
    val author: String,
    val policyId: Long,
)