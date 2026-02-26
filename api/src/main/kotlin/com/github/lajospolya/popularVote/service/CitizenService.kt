package com.github.lajospolya.popularVote.service

import com.github.lajospolya.popularVote.controller.exception.ResourceNotFoundException
import com.github.lajospolya.popularVote.dto.CitizenDto
import com.github.lajospolya.popularVote.dto.CitizenProfileDto
import com.github.lajospolya.popularVote.dto.CitizenSelfDto
import com.github.lajospolya.popularVote.dto.CreateCitizenDto
import com.github.lajospolya.popularVote.dto.DeclarePoliticianDto
import com.github.lajospolya.popularVote.dto.PageDto
import com.github.lajospolya.popularVote.dto.VerifyIdentityDto
import com.github.lajospolya.popularVote.dto.geo.PostalCodeDto
import com.github.lajospolya.popularVote.entity.Citizen
import com.github.lajospolya.popularVote.entity.CitizenPoliticalDetails
import com.github.lajospolya.popularVote.entity.PoliticianVerification
import com.github.lajospolya.popularVote.entity.Role
import com.github.lajospolya.popularVote.mapper.CitizenMapper
import com.github.lajospolya.popularVote.mapper.geo.GeoMapper
import com.github.lajospolya.popularVote.repository.CitizenPoliticalDetailsRepository
import com.github.lajospolya.popularVote.repository.CitizenRepository
import com.github.lajospolya.popularVote.repository.LevelOfPoliticsRepository
import com.github.lajospolya.popularVote.repository.PolicyRepository
import com.github.lajospolya.popularVote.repository.PoliticianVerificationRepository
import com.github.lajospolya.popularVote.repository.VoteRepository
import com.github.lajospolya.popularVote.repository.geo.ElectoralDistrictRepository
import com.github.lajospolya.popularVote.repository.geo.PostalCodeRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.util.Optional
import kotlin.math.ceil

@Service
class CitizenService(
    private val citizenRepo: CitizenRepository,
    private val citizenPoliticalDetailsRepo: CitizenPoliticalDetailsRepository,
    private val levelOfPoliticsRepo: LevelOfPoliticsRepository,
    private val politicianVerificationRepo: PoliticianVerificationRepository,
    private val policyRepo: PolicyRepository,
    private val voteRepo: VoteRepository,
    private val postalCodeRepo: PostalCodeRepository,
    private val electoralDistrictRepo: ElectoralDistrictRepository,
    private val citizenMapper: CitizenMapper,
    private val geoMapper: GeoMapper,
    private val auth0ManagementService: Auth0ManagementService,
) {
    @Value("\${roles.citizen-role-id}")
    private lateinit var citizenRoleId: String

    @Value("\${roles.read-only-citizen-role-id}")
    private lateinit var readOnlyCitizenRoleId: String

    @Value("\${roles.politician-role-id}")
    private lateinit var politicianRoleId: String

    fun getCitizens(): Flux<CitizenDto> =
        citizenRepo.findAll().flatMap { citizen ->
            val postalCodeMono = getPostalCodeDto(citizen.postalCodeId)
            citizenPoliticalDetailsRepo
                .findByCitizenId(citizen.id!!)
                .flatMap { details ->
                    postalCodeMono.map { citizenMapper.toDto(citizen, details.politicalPartyId, it.orElse(null)) }
                }.switchIfEmpty(postalCodeMono.map { citizenMapper.toDto(citizen, null, it.orElse(null)) })
        }

    fun getPoliticians(
        page: Int,
        size: Int,
        levelOfPoliticsId: Long? = null,
    ): Mono<PageDto<CitizenDto>> {
        val politiciansFlux =
            if (levelOfPoliticsId != null) {
                citizenRepo.findAllByRoleAndLevelOfPoliticsId(Role.POLITICIAN, levelOfPoliticsId, size, page.toLong() * size)
            } else {
                citizenRepo.findAllByRole(Role.POLITICIAN, size, page.toLong() * size)
            }

        val totalCountMono =
            if (levelOfPoliticsId != null) {
                citizenRepo.countByRoleAndLevelOfPoliticsId(Role.POLITICIAN, levelOfPoliticsId)
            } else {
                citizenRepo.countByRole(Role.POLITICIAN)
            }

        return totalCountMono.flatMap { totalElements ->
            politiciansFlux
                .flatMap { citizen ->
                    val postalCodeMono = getPostalCodeDto(citizen.postalCodeId)
                    citizenPoliticalDetailsRepo
                        .findByCitizenId(citizen.id!!)
                        .flatMap { details ->
                            postalCodeMono.map { citizenMapper.toDto(citizen, details.politicalPartyId, it.orElse(null)) }
                        }.switchIfEmpty(postalCodeMono.map { citizenMapper.toDto(citizen, null, it.orElse(null)) })
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

    fun getPoliticianVerifications(): Flux<CitizenDto> =
        citizenRepo.findAllPendingVerification().flatMap { citizen ->
            val postalCodeMono = getPostalCodeDto(citizen.postalCodeId)
            citizenPoliticalDetailsRepo
                .findByCitizenId(citizen.id!!)
                .flatMap { details ->
                    postalCodeMono.map { citizenMapper.toDto(citizen, details.politicalPartyId, it.orElse(null)) }
                }.switchIfEmpty(postalCodeMono.map { citizenMapper.toDto(citizen, null, it.orElse(null)) })
        }

    fun getCitizen(id: Long): Mono<CitizenProfileDto> =
        getCitizenElseThrowResourceNotFound(id)
            .flatMap { citizen ->
                val citizenId = citizen.id!!
                val policyCountMono = policyRepo.countByPublisherCitizenId(citizenId)
                val voteCountMono = voteRepo.countByCitizenId(citizenId)
                val politicalDetailsMono = citizenPoliticalDetailsRepo.findByCitizenId(citizenId)
                val levelOfPoliticsNameMono: Mono<Optional<String>> =
                    politicalDetailsMono
                        .flatMap { details ->
                            levelOfPoliticsRepo
                                .findById(details.levelOfPoliticsId)
                                .map { Optional.of(it.name) }
                        }.defaultIfEmpty(Optional.empty())
                val electoralDistrictNameMono: Mono<Optional<String>> =
                    politicalDetailsMono
                        .flatMap { details ->
                            electoralDistrictRepo
                                .findById(details.electoralDistrictId)
                                .map { Optional.of(it.name) }
                        }.defaultIfEmpty(Optional.empty())
                val postalCodeMono = getPostalCodeDto(citizen.postalCodeId)

                politicalDetailsMono
                    .flatMap { details ->
                        Mono
                            .zip(policyCountMono, voteCountMono, levelOfPoliticsNameMono, electoralDistrictNameMono, postalCodeMono)
                            .map { tuple ->
                                citizenMapper.toProfileDto(
                                    citizen,
                                    tuple.t1,
                                    tuple.t2,
                                    tuple.t3.orElse(null),
                                    tuple.t4.orElse(null),
                                    details.politicalPartyId,
                                    tuple.t5.orElse(null),
                                )
                            }
                    }.switchIfEmpty(
                        Mono
                            .zip(policyCountMono, voteCountMono, levelOfPoliticsNameMono, electoralDistrictNameMono, postalCodeMono)
                            .map { tuple ->
                                citizenMapper.toProfileDto(
                                    citizen,
                                    tuple.t1,
                                    tuple.t2,
                                    tuple.t3.orElse(null),
                                    tuple.t4.orElse(null),
                                    null,
                                    tuple.t5.orElse(null),
                                )
                            },
                    )
            }

    fun getCitizenByName(
        givenName: String,
        surname: String,
    ): Mono<CitizenDto> =
        citizenRepo
            .findByGivenNameAndSurname(givenName, surname)
            .flatMap { citizen ->
                val postalCodeMono = getPostalCodeDto(citizen.postalCodeId)
                citizenPoliticalDetailsRepo
                    .findByCitizenId(citizen.id!!)
                    .flatMap { details ->
                        postalCodeMono.map { citizenMapper.toDto(citizen, details.politicalPartyId, it.orElse(null)) }
                    }.switchIfEmpty(postalCodeMono.map { citizenMapper.toDto(citizen, null, it.orElse(null)) })
            }.switchIfEmpty {
                Mono.error(ResourceNotFoundException())
            }

    fun getCitizenByAuthId(authId: String): Mono<CitizenSelfDto> =
        citizenRepo
            .findByAuthId(authId)
            .flatMap { citizen ->
                val citizenId = citizen.id!!
                val policyCountMono = policyRepo.countByPublisherCitizenId(citizenId)
                val voteCountMono = voteRepo.countByCitizenId(citizenId)
                val verificationPendingMono = politicianVerificationRepo.existsById(citizenId)
                val politicalDetailsMono = citizenPoliticalDetailsRepo.findByCitizenId(citizenId)
                val postalCodeMono = getPostalCodeDto(citizen.postalCodeId)

                politicalDetailsMono
                    .flatMap { details ->
                        Mono
                            .zip(policyCountMono, voteCountMono, verificationPendingMono, postalCodeMono)
                            .map { tuple ->
                                citizenMapper.toSelfDto(
                                    citizen,
                                    tuple.t1,
                                    tuple.t2,
                                    tuple.t3,
                                    details.politicalPartyId,
                                    tuple.t4.orElse(null),
                                )
                            }
                    }.switchIfEmpty(
                        Mono
                            .zip(policyCountMono, voteCountMono, verificationPendingMono, postalCodeMono)
                            .map { tuple ->
                                citizenMapper.toSelfDto(citizen, tuple.t1, tuple.t2, tuple.t3, null, tuple.t4.orElse(null))
                            },
                    )
            }.switchIfEmpty {
                Mono.error(ResourceNotFoundException())
            }

    fun checkCitizenExistsByAuthId(authId: String): Mono<Boolean> =
        citizenRepo
            .findByAuthId(authId)
            .map { true }
            .switchIfEmpty(Mono.just(false))

    @Transactional
    fun saveCitizen(
        citizenDto: CreateCitizenDto,
        authId: String,
    ): Mono<CitizenDto> {
        val citizen = citizenMapper.toEntity(citizenDto, authId)
        // Must refetch citizen after saving to get its Role because it's auto-created in the database
        val savedCitizenMono = citizenRepo.save(citizen).flatMap { savedCitizen -> citizenRepo.findById(savedCitizen.id!!) }

        val addRoleMono = auth0ManagementService.addRoleToUser(authId, readOnlyCitizenRoleId)

        return Mono.zip(savedCitizenMono, addRoleMono.thenReturn(true)) { savedCitizen, _ ->
            citizenMapper.toDto(savedCitizen, null, null)
        }
    }

    fun deleteCitizen(id: Long): Mono<Void> = getCitizenElseThrowResourceNotFound(id).flatMap(citizenRepo::delete)

    fun verifyIdentity(
        authId: String,
        verifyIdentityDto: VerifyIdentityDto,
    ): Mono<CitizenSelfDto> =
        citizenRepo
            .findByAuthId(authId)
            .flatMap { citizen ->
                val updatedCitizen = citizen.copy(postalCodeId = verifyIdentityDto.postalCodeId)
                citizenRepo.save(updatedCitizen)
            }.flatMap {
                auth0ManagementService
                    .addRoleToUser(authId, citizenRoleId)
                    .then(getCitizenByAuthId(authId))
            }

    @Transactional
    fun declarePolitician(
        authId: String,
        declarePoliticianDto: DeclarePoliticianDto,
    ): Mono<Void> =
        citizenRepo
            .findByAuthId(authId)
            .flatMap { citizen ->
                val details =
                    CitizenPoliticalDetails(
                        citizenId = citizen.id!!,
                        levelOfPoliticsId = declarePoliticianDto.levelOfPoliticsId,
                        electoralDistrictId = declarePoliticianDto.electoralDistrictId,
                        politicalPartyId = declarePoliticianDto.politicalAffiliationId,
                    )
                citizenPoliticalDetailsRepo.save(details).then(politicianVerificationRepo.save(PoliticianVerification(citizen.id!!)))
            }.then()

    fun verifyPolitician(id: Long): Mono<CitizenSelfDto> =
        citizenRepo
            .findById(id)
            .flatMap { citizen ->
                if (citizen.role != Role.CITIZEN) {
                    return@flatMap Mono.error(IllegalStateException("Only citizens can be verified as politicians"))
                }
                val updatedCitizen = citizen.copy(role = Role.POLITICIAN)
                citizenRepo.save(updatedCitizen)
            }.flatMap { savedCitizen ->
                auth0ManagementService
                    .addRoleToUser(savedCitizen.authId, politicianRoleId)
                    .then(auth0ManagementService.removeRoleFromUser(savedCitizen.authId, citizenRoleId))
                    .then(politicianVerificationRepo.deleteById(savedCitizen.id!!))
                    .then(getCitizenByAuthId(savedCitizen.authId))
            }

    private fun getPostalCodeDto(postalCodeId: Int?): Mono<Optional<PostalCodeDto>> =
        if (postalCodeId != null) {
            postalCodeRepo
                .findById(postalCodeId)
                .map { Optional.of(geoMapper.toDto(it)) }
                .defaultIfEmpty(Optional.empty())
        } else {
            Mono.just(Optional.empty())
        }

    private fun getCitizenElseThrowResourceNotFound(id: Long): Mono<Citizen> =
        citizenRepo
            .findById(id)
            .switchIfEmpty {
                Mono.error(ResourceNotFoundException())
            }
}
