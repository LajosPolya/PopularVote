package com.github.lajospolya.popularVote.service

import com.github.lajospolya.popularVote.controller.exception.ResourceNotFoundException
import com.github.lajospolya.popularVote.dto.CreateOpinionDto
import com.github.lajospolya.popularVote.dto.OpinionDto
import com.github.lajospolya.popularVote.entity.Opinion
import com.github.lajospolya.popularVote.mapper.OpinionMapper
import com.github.lajospolya.popularVote.repository.OpinionRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class OpinionService(
    private val opinionRepo: OpinionRepository,
    private val opinionMapper: OpinionMapper,
    private val policyService: PolicyService,
    private val citizenRepo: com.github.lajospolya.popularVote.repository.CitizenRepository,
) {
    fun getOpinions(): Flux<OpinionDto> = opinionRepo.findAll().map(opinionMapper::toDto)

    fun getOpinionsByPolicyId(policyId: Long): Flux<OpinionDto> = opinionRepo.findByPolicyId(policyId).map(opinionMapper::toDto)

    fun getOpinion(id: Long): Mono<OpinionDto> = getOpinionElseThrowResourceNotFound(id).map(opinionMapper::toDto)

    fun createOpinion(
        opinionDto: CreateOpinionDto,
        authId: String,
    ): Mono<OpinionDto> =
        policyService
            .getPolicy(opinionDto.policyId)
            .flatMap {
                citizenRepo.findByAuthId(authId)
            }
            .switchIfEmpty {
                Mono.error(ResourceNotFoundException())
            }
            .flatMap { citizen ->
                val opinion =
                    Opinion(
                        id = null,
                        description = opinionDto.description,
                        authorId = citizen.id!!,
                        policyId = opinionDto.policyId,
                    )
                opinionRepo.save(opinion)
            }
            .map(opinionMapper::toDto)

    fun deleteOpinion(id: Long): Mono<Void> = getOpinionElseThrowResourceNotFound(id).flatMap(opinionRepo::delete)

    private fun getOpinionElseThrowResourceNotFound(id: Long): Mono<Opinion> =
        opinionRepo
            .findById(id)
            .switchIfEmpty {
                Mono.error(ResourceNotFoundException())
            }
}
