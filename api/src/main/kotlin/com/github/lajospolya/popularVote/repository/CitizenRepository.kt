package com.github.lajospolya.popularVote.repository

import com.github.lajospolya.popularVote.entity.Citizen
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface CitizenRepository : ReactiveCrudRepository<Citizen, Long> {
    fun findByGivenNameAndSurname(
        givenName: String,
        surname: String,
    ): Mono<Citizen>

    fun findByAuthId(authId: String): Mono<Citizen>
}
