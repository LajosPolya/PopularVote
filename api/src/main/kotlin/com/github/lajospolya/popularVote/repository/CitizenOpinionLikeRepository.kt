package com.github.lajospolya.popularVote.repository

import com.github.lajospolya.popularVote.entity.CitizenOpinionLike
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface CitizenOpinionLikeRepository : ReactiveCrudRepository<CitizenOpinionLike, String> {
    fun findAllByCitizenId(citizenId: Long): Flux<CitizenOpinionLike>
    fun findByCitizenIdAndOpinionId(citizenId: Long, opinionId: Long): Mono<CitizenOpinionLike>
    fun deleteByCitizenIdAndOpinionId(citizenId: Long, opinionId: Long): Mono<Void>
}
