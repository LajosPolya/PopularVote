package com.github.lajospolya.popularVote.controller

import com.github.lajospolya.popularVote.dto.LevelOfPoliticsDto
import com.github.lajospolya.popularVote.service.LevelOfPoliticsService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("levels-of-politics")
class LevelOfPoliticsController(
    private val levelOfPoliticsService: LevelOfPoliticsService,
) {
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    fun getLevelsOfPolitics(): Flux<LevelOfPoliticsDto> =
        levelOfPoliticsService.getLevelsOfPolitics()
}
