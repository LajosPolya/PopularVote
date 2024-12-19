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
    fun getCitizens(): Flux<CitizenDto> {
        return citizenRepo.findAll().map(citizenMapper::toDto)
    }

    fun getCitizen(id: Long): Mono<CitizenDto> {
        return getCitizenElseThrowResourceNotFound(id).map(citizenMapper::toDto)
            .switchIfEmpty {
                Mono.error(ResourceNotFoundException())
            }
    }

    fun saveCitizen(citizenDto: CreateCitizenDto): Mono<CitizenDto> {
        val citizen = citizenMapper.toEntity(citizenDto)
        return citizenRepo.save(citizen).map(citizenMapper::toDto)
    }

    fun deleteCitizen(id: Long): Mono<Void> {
        return getCitizenElseThrowResourceNotFound(id).flatMap(citizenRepo::delete)
    }

    private fun getCitizenElseThrowResourceNotFound(id: Long): Mono<Citizen> {
        return citizenRepo.findById(id)
            .switchIfEmpty {
                Mono.error(ResourceNotFoundException())
            }
    }
}
