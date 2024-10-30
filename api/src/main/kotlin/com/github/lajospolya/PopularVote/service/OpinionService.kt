package com.github.lajospolya.PopularVote.service

import com.github.lajospolya.PopularVote.controller.exception.ResourceNotFoundException
import com.github.lajospolya.PopularVote.dto.CreateOpinionDto
import com.github.lajospolya.PopularVote.dto.OpinionDto
import com.github.lajospolya.PopularVote.entity.Opinion
import com.github.lajospolya.PopularVote.mapper.OpinionMapper
import com.github.lajospolya.PopularVote.repository.OpinionRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class OpinionService(
    val opinionRepo: OpinionRepository,
    val opinionMapper: OpinionMapper,
    val policyService: PolicyService,
) {

    fun getOpinions(): Flux<OpinionDto> {
        return opinionRepo.findAll().map(opinionMapper::toDto)
    }

    fun getOpinion(id: Long): Mono<OpinionDto> {
        return getOpinionElseThrowResourceNotFound(id).map(opinionMapper::toDto)
    }

    fun createOpinion(opinionDto: CreateOpinionDto): Mono<OpinionDto> {
        return policyService.getPolicy(opinionDto.policyId)
            .thenReturn(opinionMapper.toEntity(opinionDto))
            .flatMap(opinionRepo::save)
            .map(opinionMapper::toDto)
    }

    fun deleteOpinion(id: Long): Mono<Void> {
        return getOpinionElseThrowResourceNotFound(id).flatMap(opinionRepo::delete)
    }

    private fun getOpinionElseThrowResourceNotFound(id: Long): Mono<Opinion> {
        return opinionRepo.findById(id)
            .switchIfEmpty {
                Mono.error(ResourceNotFoundException())
            }
    }
}