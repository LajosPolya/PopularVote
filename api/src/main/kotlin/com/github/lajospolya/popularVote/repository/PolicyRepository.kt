package com.github.lajospolya.popularVote.repository

import com.github.lajospolya.popularVote.entity.Policy
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface PolicyRepository : ReactiveCrudRepository<Policy, Long> {
    fun countByPublisherCitizenId(publisherCitizenId: Long): Mono<Long>

    @Query("SELECT p.* FROM policy p JOIN citizen c ON p.publisher_citizen_id = c.id WHERE c.political_party_id = :id")
    fun findAllByPublisherPoliticalPartyId(@Param("id") id: Int): Flux<Policy>
}
