package com.github.lajospolya.popularVote.service

import com.github.lajospolya.popularVote.controller.exception.ResourceNotFoundException
import com.github.lajospolya.popularVote.dto.CitizenDto
import com.github.lajospolya.popularVote.dto.CitizenSelfDto
import com.github.lajospolya.popularVote.dto.CreateCitizenDto
import com.github.lajospolya.popularVote.entity.Citizen
import com.github.lajospolya.popularVote.mapper.CitizenMapper
import com.github.lajospolya.popularVote.repository.CitizenRepository
import com.github.lajospolya.popularVote.repository.PolicyRepository
import com.github.lajospolya.popularVote.repository.VoteRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class CitizenService(
    private val citizenRepo: CitizenRepository,
    private val policyRepo: PolicyRepository,
    private val voteRepo: VoteRepository,
    private val citizenMapper: CitizenMapper,
) {
    fun getCitizens(): Flux<CitizenDto> = citizenRepo.findAll().map(citizenMapper::toDto)

    fun getCitizen(id: Long): Mono<CitizenDto> =
        getCitizenElseThrowResourceNotFound(id)
            .map(citizenMapper::toDto)
            .switchIfEmpty {
                Mono.error(ResourceNotFoundException())
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
                Mono.zip(
                    policyRepo.countByPublisherCitizenId(citizenId),
                    voteRepo.countByCitizenId(citizenId),
                ) { policyCount, voteCount ->
                    citizenMapper.toSelfDto(citizen, policyCount, voteCount)
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
        return citizenRepo.save(citizen).flatMap { savedCitizen -> citizenRepo.findById(savedCitizen.id!!) }.map(citizenMapper::toDto)
    }

    fun deleteCitizen(id: Long): Mono<Void> = getCitizenElseThrowResourceNotFound(id).flatMap(citizenRepo::delete)

    private fun getCitizenElseThrowResourceNotFound(id: Long): Mono<Citizen> =
        citizenRepo
            .findById(id)
            .switchIfEmpty {
                Mono.error(ResourceNotFoundException())
            }
}
