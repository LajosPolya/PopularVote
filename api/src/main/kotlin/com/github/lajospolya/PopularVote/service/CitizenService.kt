package com.github.lajospolya.PopularVote.service

import com.github.lajospolya.PopularVote.entity.Citizen
import com.github.lajospolya.PopularVote.repository.CitizenRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class CitizenService (
    val citizenRepo: CitizenRepository,
) {

    fun getCitizens(): Flux<Citizen> {
        return citizenRepo.findAll()
    }
}