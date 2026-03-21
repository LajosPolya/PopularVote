package com.github.lajospolya.popularVote.repository

import com.github.lajospolya.popularVote.dto.PolicySummaryDto
import com.github.lajospolya.popularVote.entity.Policy
import com.github.lajospolya.popularVote.entity.VotingStatus
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

interface PolicyRepositoryCustom {
    fun findAllBy(
        levelOfPoliticsId: Int?,
        provinceAndTerritoryId: Int?,
        status: VotingStatus?,
        publisherPoliticalPartyId: Int?,
        now: LocalDateTime,
        pageSize: Int,
        offset: Long,
    ): Flux<Policy>

    fun countBy(
        levelOfPoliticsId: Int?,
        provinceAndTerritoryId: Int?,
        status: VotingStatus?,
        publisherPoliticalPartyId: Int?,
        now: LocalDateTime,
    ): Mono<Long>

    fun findBookmarkedSummariesByCitizenId(citizenId: Long): Flux<PolicySummaryDto>
}
