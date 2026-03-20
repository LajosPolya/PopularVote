package com.github.lajospolya.popularVote.repository

import com.github.lajospolya.popularVote.entity.CitizenPoliticalDetails
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface CitizenPoliticalDetailsRepository : ReactiveCrudRepository<CitizenPoliticalDetails, Long> {
    fun findByCitizenId(citizenId: Long): Mono<CitizenPoliticalDetails>

    fun findAllByPoliticalPartyId(politicalPartyId: Int): Flux<CitizenPoliticalDetails>
}
