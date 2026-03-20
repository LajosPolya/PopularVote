package com.github.lajospolya.popularVote.repository

import com.github.lajospolya.popularVote.entity.Policy
import com.github.lajospolya.popularVote.entity.PolicyStatus
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

interface PolicyRepositoryCustom {
    fun findAllBy(
        levelOfPoliticsId: Int?,
        provinceAndTerritoryId: Int?,
        status: PolicyStatus?,
        publisherPoliticalPartyId: Int?,
        now: LocalDateTime,
        pageSize: Int,
        offset: Long,
    ): Flux<Policy>

    fun countBy(
        levelOfPoliticsId: Int?,
        provinceAndTerritoryId: Int?,
        status: PolicyStatus?,
        publisherPoliticalPartyId: Int?,
        now: LocalDateTime,
    ): Mono<Long>
}
