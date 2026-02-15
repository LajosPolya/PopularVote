package com.github.lajospolya.popularVote.repository

import com.github.lajospolya.popularVote.entity.Policy
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface PolicyRepository : ReactiveCrudRepository<Policy, Long> {
    fun countByPublisherCitizenId(publisherCitizenId: Long): Mono<Long>

    @Query(
        "SELECT * FROM policy WHERE publisher_citizen_id IN (SELECT citizen_id FROM citizen_political_details WHERE political_party_id = :id)",
    )
    fun findAllByPublisherPoliticalPartyId(id: Int): Flux<Policy>

    fun findAllByLevelOfPoliticsId(levelOfPoliticsId: Long): Flux<Policy>
}
