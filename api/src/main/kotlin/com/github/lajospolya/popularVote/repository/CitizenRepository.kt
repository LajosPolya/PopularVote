package com.github.lajospolya.popularVote.repository

import com.github.lajospolya.popularVote.entity.Citizen
import com.github.lajospolya.popularVote.entity.Role
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface CitizenRepository : ReactiveCrudRepository<Citizen, Long> {
    fun findByGivenNameAndSurname(
        givenName: String,
        surname: String,
    ): Mono<Citizen>

    fun findByAuthId(authId: String): Mono<Citizen>
    fun findAllByRole(role: Role): Flux<Citizen>

    @Query("SELECT c.* FROM citizen c JOIN politician_verification pv ON c.id = pv.citizen_id")
    fun findAllPendingVerification(): Flux<Citizen>
}
