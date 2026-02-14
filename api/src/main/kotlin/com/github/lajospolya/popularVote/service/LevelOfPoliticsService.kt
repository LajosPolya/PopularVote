package com.github.lajospolya.popularVote.service

import com.github.lajospolya.popularVote.dto.LevelOfPoliticsDto
import com.github.lajospolya.popularVote.mapper.LevelOfPoliticsMapper
import com.github.lajospolya.popularVote.repository.LevelOfPoliticsRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class LevelOfPoliticsService(
    private val levelOfPoliticsRepo: LevelOfPoliticsRepository,
    private val levelOfPoliticsMapper: LevelOfPoliticsMapper,
) {
    fun getLevelsOfPolitics(): Flux<LevelOfPoliticsDto> = levelOfPoliticsRepo.findAll().map(levelOfPoliticsMapper::toDto)
}
