package com.github.lajospolya.popularVote.service

import com.github.lajospolya.popularVote.controller.exception.ResourceNotFoundException
import com.github.lajospolya.popularVote.dto.CitizenDto
import com.github.lajospolya.popularVote.dto.CreatePoliticalPartyDto
import com.github.lajospolya.popularVote.dto.PageDto
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
import kotlin.math.ceil

@Service
class PoliticalPartyService(
    private val politicalPartyRepo: PoliticalPartyRepository,
    private val citizenRepo: CitizenRepository,
    private val citizenPoliticalDetailsRepo: CitizenPoliticalDetailsRepository,
    private val politicalPartyMapper: PoliticalPartyMapper,
    private val citizenMapper: CitizenMapper,
) {
    fun getAllPoliticalParties(
        levelOfPoliticsId: Long? = null,
        provinceAndTerritoryId: Int? = null,
    ): Flux<PoliticalPartyDto> {
        val parties =
            when {
                levelOfPoliticsId != null && provinceAndTerritoryId != null ->
                    politicalPartyRepo.findAllByLevelOfPoliticsIdAndProvinceAndTerritoryId(
                        levelOfPoliticsId,
                        provinceAndTerritoryId,
                    )
                levelOfPoliticsId != null ->
                    politicalPartyRepo.findAllByLevelOfPoliticsId(levelOfPoliticsId)
                provinceAndTerritoryId != null ->
                    politicalPartyRepo.findAllByProvinceAndTerritoryId(provinceAndTerritoryId)
                else ->
                    politicalPartyRepo.findAll()
            }
        return parties.map(politicalPartyMapper::toDto)
    }

    fun getPoliticalParties(
        page: Int,
        size: Int,
        levelOfPoliticsId: Long? = null,
        provinceAndTerritoryId: Int? = null,
    ): Mono<PageDto<PoliticalPartyDto>> {
        val parties =
            when {
                levelOfPoliticsId != null && provinceAndTerritoryId != null ->
                    politicalPartyRepo.findAllByLevelOfPoliticsIdAndProvinceAndTerritoryId(
                        levelOfPoliticsId,
                        provinceAndTerritoryId,
                        size,
                        page.toLong() * size,
                    )
                levelOfPoliticsId != null ->
                    politicalPartyRepo.findAllByLevelOfPoliticsId(levelOfPoliticsId, size, page.toLong() * size)
                provinceAndTerritoryId != null ->
                    politicalPartyRepo.findAllByProvinceAndTerritoryId(provinceAndTerritoryId, size, page.toLong() * size)
                else ->
                    politicalPartyRepo.findAllBy(size, page.toLong() * size)
            }

        val totalCountMono =
            when {
                levelOfPoliticsId != null && provinceAndTerritoryId != null ->
                    politicalPartyRepo.countByLevelOfPoliticsIdAndProvinceAndTerritoryId(levelOfPoliticsId, provinceAndTerritoryId)
                levelOfPoliticsId != null ->
                    politicalPartyRepo.countByLevelOfPoliticsId(levelOfPoliticsId)
                provinceAndTerritoryId != null ->
                    politicalPartyRepo.countByProvinceAndTerritoryId(provinceAndTerritoryId)
                else ->
                    politicalPartyRepo.count()
            }

        return totalCountMono.flatMap { totalElements ->
            parties
                .map(politicalPartyMapper::toDto)
                .collectList()
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
                        levelOfPoliticsId = createPoliticalPartyDto.levelOfPoliticsId,
                        provinceAndTerritoryId = createPoliticalPartyDto.provinceAndTerritoryId,
                    )
                politicalPartyRepo.save(updatedPoliticalParty)
            }.map(politicalPartyMapper::toDto)

    fun deletePoliticalParty(id: Int): Mono<Void> = getPoliticalPartyElseThrowResourceNotFound(id).flatMap(politicalPartyRepo::delete)

    private fun getPoliticalPartyElseThrowResourceNotFound(id: Int): Mono<PoliticalParty> =
        politicalPartyRepo.findById(id).switchIfEmpty {
            Mono.error(ResourceNotFoundException())
        }
}
