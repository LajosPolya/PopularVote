package com.github.lajospolya.PopularVote.service

import com.github.lajospolya.PopularVote.dto.CitizenDto
import com.github.lajospolya.PopularVote.mapper.CitizenMapper
import com.github.lajospolya.PopularVote.repository.CitizenRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class CitizenService (
    val citizenRepo: CitizenRepository,
    val citizenMapper: CitizenMapper,
) {

    fun getCitizens(): Flux<CitizenDto> {
        return citizenRepo.findAll().map {
            citizenMapper.citizenToDto(it)
        }
    }
}