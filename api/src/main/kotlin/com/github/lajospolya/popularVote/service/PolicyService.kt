package com.github.lajospolya.popularVote.service

import com.github.lajospolya.popularVote.controller.exception.ResourceNotFoundException
import com.github.lajospolya.popularVote.dto.CreatePolicyDto
import com.github.lajospolya.popularVote.dto.OpinionDetailsDto
import com.github.lajospolya.popularVote.dto.PolicyDetailsDto
import com.github.lajospolya.popularVote.dto.PolicyDto
import com.github.lajospolya.popularVote.entity.Policy
import com.github.lajospolya.popularVote.mapper.PolicyMapper
import com.github.lajospolya.popularVote.repository.OpinionRepository
import com.github.lajospolya.popularVote.repository.PolicyRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class PolicyService(
    private val policyRepo: PolicyRepository,
    private val policyMapper: PolicyMapper,
    private val citizenRepo: com.github.lajospolya.popularVote.repository.CitizenRepository,
    private val opinionRepo: OpinionRepository,
) {
    fun getPolicies(): Flux<PolicyDto> = policyRepo.findAll().map(policyMapper::toDto)

    fun getPolicy(id: Long): Mono<PolicyDto> = getPolicyElseThrowResourceNotFound(id).map(policyMapper::toDto)

    fun getPolicyDetails(id: Long): Mono<PolicyDetailsDto> =
        getPolicyElseThrowResourceNotFound(id).flatMap { policy ->
            Mono.zip(
                citizenRepo.findById(policy.publisherCitizenId),
                opinionRepo.findByPolicyId(policy.id!!).flatMap { opinion ->
                    citizenRepo.findById(opinion.authorId).map { author ->
                        OpinionDetailsDto(
                            id = opinion.id!!,
                            description = opinion.description,
                            authorId = opinion.authorId,
                            authorName = "${author.givenName} ${author.surname}",
                            policyId = opinion.policyId,
                        )
                    }
                }.collectList(),
            ).map { tuple ->
                val publisher = tuple.t1
                val opinions = tuple.t2
                PolicyDetailsDto(
                    id = policy.id!!,
                    description = policy.description,
                    publisherCitizenId = policy.publisherCitizenId,
                    publisherName = "${publisher.givenName} ${publisher.surname}",
                    opinions = opinions,
                )
            }
        }

    fun createPolicy(policyDto: CreatePolicyDto, publisherCitizenId: Long): Mono<PolicyDto> {
        val policy = policyMapper.toEntity(policyDto, publisherCitizenId)
        return policyRepo.save(policy).map(policyMapper::toDto)
    }

    fun deletePolicy(id: Long): Mono<Void> = getPolicyElseThrowResourceNotFound(id).flatMap(policyRepo::delete)

    private fun getPolicyElseThrowResourceNotFound(id: Long): Mono<Policy> =
        policyRepo
            .findById(id)
            .switchIfEmpty {
                Mono.error(ResourceNotFoundException())
            }
}
