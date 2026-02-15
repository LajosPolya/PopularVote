package com.github.lajospolya.popularVote.service

import com.github.lajospolya.popularVote.controller.exception.ResourceNotFoundException
import com.github.lajospolya.popularVote.dto.CitizenDto
import com.github.lajospolya.popularVote.dto.CreatePoliticalPartyDto
import com.github.lajospolya.popularVote.dto.PoliticalPartyDto
import com.github.lajospolya.popularVote.entity.PoliticalParty
import com.github.lajospolya.popularVote.entity.Role
import com.github.lajospolya.popularVote.mapper.CitizenMapper
import com.github.lajospolya.popularVote.mapper.PoliticalPartyMapper
import com.github.lajospolya.popularVote.repository.CitizenPoliticalDetailsRepository
import com.github.lajospolya.popularVote.repository.CitizenRepository
import com.github.lajospolya.popularVote.repository.PoliticalPartyRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class PoliticalPartyService(
    private val politicalPartyRepo: PoliticalPartyRepository,
    private val citizenRepo: CitizenRepository,
    private val citizenPoliticalDetailsRepo: CitizenPoliticalDetailsRepository,
    private val politicalPartyMapper: PoliticalPartyMapper,
    private val citizenMapper: CitizenMapper,
) {
    fun getPoliticalParties(): Flux<PoliticalPartyDto> = politicalPartyRepo.findAll().map(politicalPartyMapper::toDto)

    fun getPoliticalParty(id: Int): Mono<PoliticalPartyDto> =
        getPoliticalPartyElseThrowResourceNotFound(id).map(politicalPartyMapper::toDto)

    fun getPoliticalPartyMembers(id: Int): Flux<CitizenDto> =
        citizenRepo
            .findAllByPoliticalPartyIdAndRole(id, Role.POLITICIAN)
            .flatMap { citizen ->
                citizenPoliticalDetailsRepo
                    .findByCitizenId(citizen.id!!)
                    .map { details -> citizenMapper.toDto(citizen, details.politicalPartyId) }
                    .defaultIfEmpty(citizenMapper.toDto(citizen, null))
            }

    fun createPoliticalParty(createPoliticalPartyDto: CreatePoliticalPartyDto): Mono<PoliticalPartyDto> {
        val politicalParty = politicalPartyMapper.toEntity(createPoliticalPartyDto)
        return politicalPartyRepo.save(politicalParty).map(politicalPartyMapper::toDto)
    }

    fun updatePoliticalParty(
        id: Int,
        createPoliticalPartyDto: CreatePoliticalPartyDto,
    ): Mono<PoliticalPartyDto> =
        getPoliticalPartyElseThrowResourceNotFound(id)
            .flatMap { politicalParty ->
                val updatedPoliticalParty =
                    politicalParty.copy(
                        displayName = createPoliticalPartyDto.displayName,
                        hexColor = createPoliticalPartyDto.hexColor,
                        description = createPoliticalPartyDto.description,
                    )
                politicalPartyRepo.save(updatedPoliticalParty)
            }.map(politicalPartyMapper::toDto)

    fun deletePoliticalParty(id: Int): Mono<Void> = getPoliticalPartyElseThrowResourceNotFound(id).flatMap(politicalPartyRepo::delete)

    private fun getPoliticalPartyElseThrowResourceNotFound(id: Int): Mono<PoliticalParty> =
        politicalPartyRepo.findById(id).switchIfEmpty {
            Mono.error(ResourceNotFoundException())
        }
}
