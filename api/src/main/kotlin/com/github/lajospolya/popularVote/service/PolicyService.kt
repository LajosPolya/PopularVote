package com.github.lajospolya.popularVote.service

import com.github.lajospolya.popularVote.controller.exception.ResourceNotFoundException
import com.github.lajospolya.popularVote.dto.CitizenDto
import com.github.lajospolya.popularVote.dto.CreatePolicyDto
import com.github.lajospolya.popularVote.dto.OpinionDetailsDto
import com.github.lajospolya.popularVote.dto.PageDto
import com.github.lajospolya.popularVote.dto.PolicyDetailsDto
import com.github.lajospolya.popularVote.dto.PolicyDto
import com.github.lajospolya.popularVote.dto.PolicySummaryDto
import com.github.lajospolya.popularVote.entity.Policy
import com.github.lajospolya.popularVote.entity.PolicyBookmark
import com.github.lajospolya.popularVote.entity.PolicyCoAuthorCitizen
import com.github.lajospolya.popularVote.mapper.CitizenMapper
import com.github.lajospolya.popularVote.mapper.PolicyMapper
import com.github.lajospolya.popularVote.repository.CitizenPoliticalDetailsRepository
import com.github.lajospolya.popularVote.repository.CitizenRepository
import com.github.lajospolya.popularVote.repository.OpinionRepository
import com.github.lajospolya.popularVote.repository.PolicyBookmarkRepository
import com.github.lajospolya.popularVote.repository.PolicyCoAuthorCitizenRepository
import com.github.lajospolya.popularVote.repository.PolicyRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import kotlin.math.ceil

@Service
class PolicyService(
    private val policyRepo: PolicyRepository,
    private val policyCoAuthorCitizenRepo: PolicyCoAuthorCitizenRepository,
    private val policyBookmarkRepo: PolicyBookmarkRepository,
    private val policyMapper: PolicyMapper,
    private val citizenRepo: CitizenRepository,
    private val citizenMapper: CitizenMapper,
    private val opinionRepo: OpinionRepository,
    private val citizenPoliticalDetailsRepo: CitizenPoliticalDetailsRepository,
) {
    fun getPolicies(
        currentCitizenAuthId: String,
        page: Int,
        size: Int,
        levelOfPoliticsId: Int? = null,
    ): Mono<PageDto<PolicySummaryDto>> {
        val policiesFlux =
            if (levelOfPoliticsId != null) {
                policyRepo.findAllByLevelOfPoliticsId(levelOfPoliticsId, size, page.toLong() * size)
            } else {
                policyRepo.findAllBy(size, page.toLong() * size)
            }

        val totalCountMono =
            if (levelOfPoliticsId != null) {
                policyRepo.countByLevelOfPoliticsId(levelOfPoliticsId)
            } else {
                policyRepo.count()
            }

        return totalCountMono.flatMap { totalElements ->
            policiesFlux
                .concatMap { policy ->
                    getPolicySummary(policy.id!!, currentCitizenAuthId)
                }.collectList()
                .map { content ->
                    PageDto(
                        content = content,
                        totalElements = totalElements,
                        totalPages = ceil(totalElements.toDouble() / size).toInt(),
                        pageNumber = page,
                        pageSize = size,
                    )
                }
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
                    citizenPoliticalDetailsRepo.findByCitizenId(policy.publisherCitizenId),
                    getCoAuthorsForPolicy(policy.id!!).collectList(),
                    opinionRepo
                        .findByPolicyId(policy.id!!)
                        .flatMap { opinion ->
                            citizenRepo.findById(opinion.authorId).flatMap { author ->
                                citizenPoliticalDetailsRepo
                                    .findByCitizenId(author.id!!)
                                    .map { authorDetails ->
                                        OpinionDetailsDto(
                                            id = opinion.id!!,
                                            description = opinion.description,
                                            authorId = opinion.authorId,
                                            authorName = author.fullName,
                                            authorPoliticalAffiliationId = authorDetails.politicalPartyId,
                                            policyId = opinion.policyId,
                                        )
                                    }.switchIfEmpty { Mono.error { IllegalStateException("Author must have political details") } }
                            }
                        }.collectList(),
                ).map { tuple ->
                    val publisher = tuple.t1
                    val publisherDetails = tuple.t2
                    val coAuthors = tuple.t3
                    val opinions = tuple.t4
                    PolicyDetailsDto(
                        id = policy.id!!,
                        description = policy.description,
                        publisherCitizenId = policy.publisherCitizenId,
                        levelOfPoliticsId = policy.levelOfPoliticsId,
                        citizenPoliticalDetailsId = policy.citizenPoliticalDetailsId,
                        publisherName = publisher.fullName,
                        publisherPoliticalAffiliationId = publisherDetails.politicalPartyId,
                        coAuthorCitizens = coAuthors,
                        opinions = opinions,
                        closeDate = policy.closeDate,
                        creationDate = policy.creationDate!!,
                    )
                }
        }

    @Transactional
    fun createPolicy(
        policyDto: CreatePolicyDto,
        publisherCitizenId: Long,
    ): Mono<PolicyDto> =
        citizenRepo.findById(publisherCitizenId).flatMap { publisher ->
            citizenPoliticalDetailsRepo
                .findByCitizenId(publisherCitizenId)
                .flatMap { details ->
                    val policy =
                        policyMapper.toEntity(
                            policyDto,
                            publisherCitizenId,
                            details.levelOfPoliticsId,
                            details.id!!,
                        )
                    policyRepo.save(policy).flatMap { savedPolicy ->
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
                }.switchIfEmpty {
                    Mono.error(IllegalStateException("Publisher must have political details to create a policy"))
                }
        }

    private fun getCoAuthorsForPolicy(policyId: Long): Flux<CitizenDto> =
        policyCoAuthorCitizenRepo
            .findByPolicyId(policyId)
            .flatMap { pac ->
                citizenRepo.findById(pac.citizenId).flatMap { citizen ->
                    citizenPoliticalDetailsRepo
                        .findByCitizenId(citizen.id!!)
                        .map { details -> citizenMapper.toDto(citizen, details.politicalPartyId) }
                        .defaultIfEmpty(citizenMapper.toDto(citizen, null))
                }
            }

    fun deletePolicy(id: Long): Mono<Void> = getPolicyElseThrowResourceNotFound(id).flatMap(policyRepo::delete)

    fun getPoliciesByPoliticalPartyId(
        politicalPartyId: Int,
        currentCitizenAuthId: String,
    ): Flux<PolicySummaryDto> =
        policyRepo.findAllByPublisherPoliticalPartyId(politicalPartyId).concatMap { policy ->
            getPolicySummary(policy.id!!, currentCitizenAuthId)
        }

    fun getPoliciesByPublisherCitizenId(
        publisherCitizenId: Long,
        currentCitizenAuthId: String,
    ): Flux<PolicySummaryDto> =
        policyRepo.findAllByPublisherCitizenIdOrderByCreationDateDescIdDesc(publisherCitizenId).concatMap { policy ->
            getPolicySummary(policy.id!!, currentCitizenAuthId)
        }

    fun bookmarkPolicy(
        policyId: Long,
        citizenId: Long,
    ): Mono<Void> =
        getPolicyElseThrowResourceNotFound(policyId)
            .flatMap {
                policyBookmarkRepo.save(PolicyBookmark(policyId, citizenId))
            }.then()

    fun getBookmarkedPolicies(citizenAuthId: String): Flux<PolicySummaryDto> =
        citizenRepo.findByAuthId(citizenAuthId).flatMapMany { citizen ->
            policyBookmarkRepo
                .findByCitizenId(citizen.id!!)
                .flatMap { bookmark ->
                    getPolicySummary(bookmark.policyId, citizenAuthId)
                }.collectList()
                .flatMapMany { list ->
                    Flux.fromIterable(list.sortedByDescending { it.id })
                }
        }

    fun getPolicySummary(
        id: Long,
        currentCitizenAuthId: String,
    ): Mono<PolicySummaryDto> =
        getPolicyElseThrowResourceNotFound(id).flatMap { policy ->
            Mono
                .zip(
                    citizenRepo.findById(policy.publisherCitizenId),
                    isPolicyBookmarked(policy.id!!, currentCitizenAuthId),
                    citizenPoliticalDetailsRepo.findByCitizenId(policy.publisherCitizenId).switchIfEmpty {
                        Mono.error(
                            IllegalStateException("Publisher must have political details to create a policy ${policy.id}"),
                        )
                    },
                ).map { tuple ->
                    val publisher = tuple.t1
                    val isBookmarked = tuple.t2
                    val publisherDetails = tuple.t3
                    policyMapper.toSummaryDto(
                        policy = policy,
                        publisherName = publisher.fullName,
                        isBookmarked = isBookmarked,
                        publisherPoliticalPartyId = publisherDetails?.politicalPartyId,
                    )
                }
        }

    fun isPolicyBookmarked(
        policyId: Long,
        citizenAuthId: String,
    ): Mono<Boolean> =
        citizenRepo
            .findByAuthId(citizenAuthId)
            .flatMap { citizen ->
                policyBookmarkRepo.findByPolicyIdAndCitizenId(policyId, citizen.id!!).map { true }.switchIfEmpty(Mono.just(false))
            }.switchIfEmpty(Mono.just(false))

    fun unbookmarkPolicy(
        policyId: Long,
        citizenId: Long,
    ): Mono<Void> = policyBookmarkRepo.deleteByPolicyIdAndCitizenId(policyId, citizenId)

    private fun getPolicyElseThrowResourceNotFound(id: Long): Mono<Policy> =
        policyRepo
            .findById(id)
            .switchIfEmpty {
                Mono.error(ResourceNotFoundException())
            }
}
