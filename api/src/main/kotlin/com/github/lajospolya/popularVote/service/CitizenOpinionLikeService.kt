package com.github.lajospolya.popularVote.service

import com.github.lajospolya.popularVote.controller.exception.ResourceNotFoundException
import com.github.lajospolya.popularVote.dto.OpinionLikeCountDto
import com.github.lajospolya.popularVote.entity.CitizenOpinionLike
import com.github.lajospolya.popularVote.repository.CitizenOpinionLikeRepository
import com.github.lajospolya.popularVote.repository.CitizenRepository
import com.github.lajospolya.popularVote.repository.OpinionRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class CitizenOpinionLikeService(
    private val citizenOpinionLikeRepo: CitizenOpinionLikeRepository,
    private val citizenRepo: CitizenRepository,
    private val opinionRepo: OpinionRepository,
) {
    fun likeOpinion(
        authId: String,
        opinionId: Long,
    ): Mono<CitizenOpinionLike> =
        citizenRepo
            .findByAuthId(authId)
            .flatMap { citizen ->
                opinionRepo
                    .findById(opinionId)
                    .flatMap {
                        citizenOpinionLikeRepo.save(CitizenOpinionLike(citizen.id!!, opinionId))
                    }.switchIfEmpty {
                        Mono.error(ResourceNotFoundException())
                    }
            }.switchIfEmpty {
                Mono.error(ResourceNotFoundException())
            }

    fun unlikeOpinion(
        authId: String,
        opinionId: Long,
    ): Mono<Void> =
        citizenRepo
            .findByAuthId(authId)
            .flatMap { citizen ->
                citizenOpinionLikeRepo.deleteByCitizenIdAndOpinionId(citizen.id!!, opinionId)
            }

    fun getLikedOpinionIds(authId: String): Flux<Long> =
        citizenRepo
            .findByAuthId(authId)
            .flatMapMany { citizen ->
                citizenOpinionLikeRepo
                    .findAllByCitizenId(citizen.id!!)
                    .map { it.opinionId }
            }

    fun getOpinionLikeCounts(opinionIds: List<Long>): Flux<OpinionLikeCountDto> = citizenOpinionLikeRepo.countLikesForOpinions(opinionIds)
}
