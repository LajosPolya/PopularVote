package com.github.lajospolya.popularVote.repository

import com.github.lajospolya.popularVote.dto.OpinionLikeCountDto
import com.github.lajospolya.popularVote.entity.CitizenOpinionLike
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface CitizenOpinionLikeRepository : ReactiveCrudRepository<CitizenOpinionLike, String> {
    fun findAllByCitizenId(citizenId: Long): Flux<CitizenOpinionLike>
    fun findByCitizenIdAndOpinionId(citizenId: Long, opinionId: Long): Mono<CitizenOpinionLike>
    fun deleteByCitizenIdAndOpinionId(citizenId: Long, opinionId: Long): Mono<Void>

    @Query("SELECT opinion_id, COUNT(citizen_id) as like_count FROM citizen_opinion_like WHERE opinion_id IN (:opinionIds) GROUP BY opinion_id")
    fun countLikesForOpinions(opinionIds: List<Long>): Flux<OpinionLikeCountDto>
}
