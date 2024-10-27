package com.github.lajospolya.PopularVote.service

import com.github.lajospolya.PopularVote.dto.CitizenDto
import com.github.lajospolya.PopularVote.dto.CreateCitizenDto
import com.github.lajospolya.PopularVote.entity.Citizen
import com.github.lajospolya.PopularVote.mapper.CitizenMapper
import com.github.lajospolya.PopularVote.repository.CitizenRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class CitizenService (
    val citizenRepo: CitizenRepository,
    val citizenMapper: CitizenMapper,
) {

    fun getCitizens(): Flux<CitizenDto> {
        return citizenRepo.findAll().map(citizenMapper::entityToDto)
    }

    fun getCitizen(id: Long): Mono<CitizenDto> {
        return citizenRepo.findById(id).map(citizenMapper::entityToDto)
    }

    fun saveCitizen(citizenDto: CreateCitizenDto): Mono<CitizenDto> {
        val citizen = citizenMapper.toEntity(citizenDto)
        return citizenRepo.save(citizen).map(citizenMapper::entityToDto)
    }

    fun deleteCitizen(id: Long): Mono<Void> {
        return citizenRepo.deleteById(id)
    }
}