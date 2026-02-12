package com.github.lajospolya.popularVote.service

import com.github.lajospolya.popularVote.controller.exception.ResourceNotFoundException
import com.github.lajospolya.popularVote.dto.CreatePoliticalPartyDto
import com.github.lajospolya.popularVote.dto.PoliticalPartyDto
import com.github.lajospolya.popularVote.entity.PoliticalParty
import com.github.lajospolya.popularVote.mapper.PoliticalPartyMapper
import com.github.lajospolya.popularVote.repository.PoliticalPartyRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class PoliticalPartyService(
    private val politicalPartyRepo: PoliticalPartyRepository,
    private val politicalPartyMapper: PoliticalPartyMapper,
) {
    fun getPoliticalParties(): Flux<PoliticalPartyDto> =
        politicalPartyRepo.findAll().map(politicalPartyMapper::toDto)

    fun getPoliticalParty(id: Int): Mono<PoliticalPartyDto> =
        getPoliticalPartyElseThrowResourceNotFound(id).map(politicalPartyMapper::toDto)

    fun createPoliticalParty(createPoliticalPartyDto: CreatePoliticalPartyDto): Mono<PoliticalPartyDto> {
        val politicalParty = politicalPartyMapper.toEntity(createPoliticalPartyDto)
        return politicalPartyRepo.save(politicalParty).map(politicalPartyMapper::toDto)
    }

    fun updatePoliticalParty(id: Int, createPoliticalPartyDto: CreatePoliticalPartyDto): Mono<PoliticalPartyDto> =
        getPoliticalPartyElseThrowResourceNotFound(id).flatMap { politicalParty ->
            val updatedPoliticalParty = politicalParty.copy(
                name = createPoliticalPartyDto.name,
                displayName = createPoliticalPartyDto.displayName
            )
            politicalPartyRepo.save(updatedPoliticalParty)
        }.map(politicalPartyMapper::toDto)

    fun deletePoliticalParty(id: Int): Mono<Void> =
        getPoliticalPartyElseThrowResourceNotFound(id).flatMap(politicalPartyRepo::delete)

    private fun getPoliticalPartyElseThrowResourceNotFound(id: Int): Mono<PoliticalParty> =
        politicalPartyRepo.findById(id).switchIfEmpty {
            Mono.error(ResourceNotFoundException())
        }
}
