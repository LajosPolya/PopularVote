package com.github.lajospolya.popularVote.service

import com.github.lajospolya.popularVote.controller.exception.ResourceNotFoundException
import com.github.lajospolya.popularVote.dto.CitizenDto
import com.github.lajospolya.popularVote.dto.CitizenProfileDto
import com.github.lajospolya.popularVote.dto.CitizenSelfDto
import com.github.lajospolya.popularVote.dto.CreateCitizenDto
import com.github.lajospolya.popularVote.dto.DeclarePoliticianDto
import com.github.lajospolya.popularVote.entity.Citizen
import com.github.lajospolya.popularVote.entity.CitizenPoliticalDetails
import com.github.lajospolya.popularVote.entity.PoliticianVerification
import com.github.lajospolya.popularVote.entity.Role
import com.github.lajospolya.popularVote.mapper.CitizenMapper
import com.github.lajospolya.popularVote.repository.CitizenPoliticalDetailsRepository
import com.github.lajospolya.popularVote.repository.CitizenRepository
import com.github.lajospolya.popularVote.repository.LevelOfPoliticsRepository
import com.github.lajospolya.popularVote.repository.PolicyRepository
import com.github.lajospolya.popularVote.repository.PoliticianVerificationRepository
import com.github.lajospolya.popularVote.repository.VoteRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.util.Optional

@Service
class CitizenService(
    private val citizenRepo: CitizenRepository,
    private val citizenPoliticalDetailsRepo: CitizenPoliticalDetailsRepository,
    private val levelOfPoliticsRepo: LevelOfPoliticsRepository,
    private val politicianVerificationRepo: PoliticianVerificationRepository,
    private val policyRepo: PolicyRepository,
    private val voteRepo: VoteRepository,
    private val citizenMapper: CitizenMapper,
    private val auth0ManagementService: Auth0ManagementService,
) {
    @Value("\${roles.citizen-role-id}")
    private lateinit var citizenRoleId: String

    @Value("\${roles.politician-role-id}")
    private lateinit var politicianRoleId: String

    fun getCitizens(): Flux<CitizenDto> = citizenRepo.findAll().map(citizenMapper::toDto)

    fun getPoliticians(levelOfPoliticsId: Long? = null): Flux<CitizenDto> {
        val politiciansFlux =
            if (levelOfPoliticsId != null) {
                citizenRepo.findAllByRoleAndLevelOfPoliticsId(Role.POLITICIAN, levelOfPoliticsId)
            } else {
                citizenRepo.findAllByRole(Role.POLITICIAN)
            }
        return politiciansFlux.map(citizenMapper::toDto)
    }

    fun getPoliticianVerifications(): Flux<CitizenDto> = citizenRepo.findAllPendingVerification().map(citizenMapper::toDto)

    fun getCitizen(id: Long): Mono<CitizenProfileDto> =
        getCitizenElseThrowResourceNotFound(id)
            .flatMap { citizen ->
                val citizenId = citizen.id!!
                val policyCountMono = policyRepo.countByPublisherCitizenId(citizenId)
                val voteCountMono = voteRepo.countByCitizenId(citizenId)
                val levelOfPoliticsNameMono: Mono<Optional<String>> =
                    citizenPoliticalDetailsRepo
                        .findByCitizenId(citizenId)
                        .flatMap { details ->
                            levelOfPoliticsRepo
                                .findById(details.levelOfPoliticsId)
                                .map { Optional.of(it.name) }
                        }.defaultIfEmpty(Optional.empty())

                Mono
                    .zip(policyCountMono, voteCountMono, levelOfPoliticsNameMono)
                    .map { tuple ->
                        citizenMapper.toProfileDto(citizen, tuple.t1, tuple.t2, tuple.t3.orElse(null))
                    }
            }

    fun getCitizenByName(
        givenName: String,
        surname: String,
    ): Mono<CitizenDto> =
        citizenRepo
            .findByGivenNameAndSurname(givenName, surname)
            .map(citizenMapper::toDto)
            .switchIfEmpty {
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

                Mono
                    .zip(policyCountMono, voteCountMono, verificationPendingMono)
                    .map { tuple ->
                        citizenMapper.toSelfDto(citizen, tuple.t1, tuple.t2, tuple.t3)
                    }
            }.switchIfEmpty {
                Mono.error(ResourceNotFoundException())
            }

    fun checkCitizenExistsByAuthId(authId: String): Mono<Boolean> =
        citizenRepo
            .findByAuthId(authId)
            .map { true }
            .switchIfEmpty(Mono.just(false))

    fun saveCitizen(
        citizenDto: CreateCitizenDto,
        authId: String,
    ): Mono<CitizenDto> {
        val citizen = citizenMapper.toEntity(citizenDto, authId)
        // Must refetch citizen after saving to get its Role because it's auto-created in the database
        val savedCitizenMono = citizenRepo.save(citizen).flatMap { savedCitizen -> citizenRepo.findById(savedCitizen.id!!) }

        val addRoleMono = auth0ManagementService.addRoleToUser(authId, citizenRoleId)

        return Mono.zip(savedCitizenMono, addRoleMono.thenReturn(true)) { savedCitizen, _ ->
            citizenMapper.toDto(savedCitizen)
        }
    }

    fun deleteCitizen(id: Long): Mono<Void> = getCitizenElseThrowResourceNotFound(id).flatMap(citizenRepo::delete)

    @Transactional
    fun declarePolitician(
        authId: String,
        declarePoliticianDto: DeclarePoliticianDto,
    ): Mono<Void> =
        citizenRepo
            .findByAuthId(authId)
            .flatMap { citizen ->
                citizenPoliticalDetailsRepo
                    .findByCitizenId(citizen.id!!)
                    .flatMap { existingDetails ->
                        val updatedDetails =
                            existingDetails.copy(
                                levelOfPoliticsId = declarePoliticianDto.levelOfPoliticsId,
                                geographicLocation = declarePoliticianDto.geographicLocation,
                            )
                        citizenPoliticalDetailsRepo.save(updatedDetails)
                    }.switchIfEmpty {
                        val details =
                            CitizenPoliticalDetails(
                                citizenId = citizen.id!!,
                                levelOfPoliticsId = declarePoliticianDto.levelOfPoliticsId,
                                geographicLocation = declarePoliticianDto.geographicLocation,
                            )
                        citizenPoliticalDetailsRepo.save(details)
                    }.then(politicianVerificationRepo.save(PoliticianVerification(citizen.id!!)))
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

    private fun getCitizenElseThrowResourceNotFound(id: Long): Mono<Citizen> =
        citizenRepo
            .findById(id)
            .switchIfEmpty {
                Mono.error(ResourceNotFoundException())
            }
}
