package com.github.lajospolya.popularVote.service

import com.github.lajospolya.popularVote.controller.exception.ResourceNotFoundException
import com.github.lajospolya.popularVote.dto.CitizenDto
import com.github.lajospolya.popularVote.dto.CreateCitizenDto
import com.github.lajospolya.popularVote.entity.Citizen
import com.github.lajospolya.popularVote.mapper.CitizenMapper
import com.github.lajospolya.popularVote.repository.CitizenRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class CitizenService(
    private val citizenRepo: CitizenRepository,
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

    fun getCitizenByAuthId(authId: String): Mono<CitizenDto> =
        citizenRepo
            .findByAuthId(authId)
            .map(citizenMapper::toDto)
            .switchIfEmpty {
                Mono.error(ResourceNotFoundException())
            }

    fun saveCitizen(citizenDto: CreateCitizenDto): Mono<CitizenDto> {
        val citizen = citizenMapper.toEntity(citizenDto)
        return citizenRepo.save(citizen).map(citizenMapper::toDto)
    }

    fun deleteCitizen(id: Long): Mono<Void> = getCitizenElseThrowResourceNotFound(id).flatMap(citizenRepo::delete)

    private fun getCitizenElseThrowResourceNotFound(id: Long): Mono<Citizen> =
        citizenRepo
            .findById(id)
            .switchIfEmpty {
                Mono.error(ResourceNotFoundException())
            }
}
