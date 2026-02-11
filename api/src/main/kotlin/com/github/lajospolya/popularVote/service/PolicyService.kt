package com.github.lajospolya.popularVote.service

import com.github.lajospolya.popularVote.controller.exception.ResourceNotFoundException
import com.github.lajospolya.popularVote.dto.CitizenDto
import com.github.lajospolya.popularVote.dto.CreatePolicyDto
import com.github.lajospolya.popularVote.dto.OpinionDetailsDto
import com.github.lajospolya.popularVote.dto.PolicyDetailsDto
import com.github.lajospolya.popularVote.dto.PolicyDto
import com.github.lajospolya.popularVote.entity.Policy
import com.github.lajospolya.popularVote.entity.PolicyCoAuthorCitizen
import com.github.lajospolya.popularVote.mapper.CitizenMapper
import com.github.lajospolya.popularVote.mapper.PolicyMapper
import com.github.lajospolya.popularVote.repository.CitizenRepository
import com.github.lajospolya.popularVote.repository.OpinionRepository
import com.github.lajospolya.popularVote.repository.PolicyCoAuthorCitizenRepository
import com.github.lajospolya.popularVote.repository.PolicyRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class PolicyService(
    private val policyRepo: PolicyRepository,
    private val policyCoAuthorCitizenRepo: PolicyCoAuthorCitizenRepository,
    private val policyMapper: PolicyMapper,
    private val citizenRepo: CitizenRepository,
    private val citizenMapper: CitizenMapper,
    private val opinionRepo: OpinionRepository,
) {
    fun getPolicies(): Flux<PolicyDto> =
        policyRepo.findAll().flatMap { policy ->
            getCoAuthorsForPolicy(policy.id!!).collectList().map { coAuthors ->
                policyMapper.toDto(policy, coAuthors)
            }
        }

    fun getPolicy(id: Long): Mono<PolicyDto> =
        getPolicyElseThrowResourceNotFound(id).flatMap { policy ->
            getCoAuthorsForPolicy(policy.id!!).collectList().map { coAuthors ->
                policyMapper.toDto(policy, coAuthors)
            }
        }

    fun getPolicyDetails(id: Long): Mono<PolicyDetailsDto> =
        getPolicyElseThrowResourceNotFound(id).flatMap { policy ->
            Mono
                .zip(
                    citizenRepo.findById(policy.publisherCitizenId),
                    getCoAuthorsForPolicy(policy.id!!).collectList(),
                    opinionRepo
                        .findByPolicyId(policy.id!!)
                        .flatMap { opinion ->
                            citizenRepo.findById(opinion.authorId).map { author ->
                                OpinionDetailsDto(
                                    id = opinion.id!!,
                                    description = opinion.description,
                                    authorId = opinion.authorId,
                                    authorName = "${author.givenName} ${author.surname}",
                                    authorPoliticalAffiliation = author.politicalAffiliation,
                                    policyId = opinion.policyId,
                                )
                            }
                        }.collectList(),
                ).map { tuple ->
                    val publisher = tuple.t1
                    val coAuthors = tuple.t2
                    val opinions = tuple.t3
                    PolicyDetailsDto(
                        id = policy.id!!,
                        description = policy.description,
                        publisherCitizenId = policy.publisherCitizenId,
                        publisherName = "${publisher.givenName} ${publisher.surname}",
                        publisherPoliticalAffiliation = publisher.politicalAffiliation,
                        coAuthorCitizens = coAuthors,
                        opinions = opinions,
                    )
                }
        }

    fun createPolicy(
        policyDto: CreatePolicyDto,
        publisherCitizenId: Long,
    ): Mono<PolicyDto> {
        val policy = policyMapper.toEntity(policyDto, publisherCitizenId)
        return policyRepo.save(policy).flatMap { savedPolicy ->
            val coAuthorsFlux =
                Flux
                    .fromIterable(policyDto.coAuthorCitizenIds)
                    .flatMap { coAuthorId ->
                        policyCoAuthorCitizenRepo.save(PolicyCoAuthorCitizen(savedPolicy.id!!, coAuthorId))
                    }
            coAuthorsFlux.collectList().flatMap {
                getCoAuthorsForPolicy(savedPolicy.id!!).collectList().map { coAuthors ->
                    policyMapper.toDto(savedPolicy, coAuthors)
                }
            }
        }
    }

    private fun getCoAuthorsForPolicy(policyId: Long): Flux<CitizenDto> =
        policyCoAuthorCitizenRepo
            .findByPolicyId(policyId)
            .flatMap { pac ->
                citizenRepo.findById(pac.citizenId).map(citizenMapper::toDto)
            }

    fun deletePolicy(id: Long): Mono<Void> = getPolicyElseThrowResourceNotFound(id).flatMap(policyRepo::delete)

    private fun getPolicyElseThrowResourceNotFound(id: Long): Mono<Policy> =
        policyRepo
            .findById(id)
            .switchIfEmpty {
                Mono.error(ResourceNotFoundException())
            }
}
