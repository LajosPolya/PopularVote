package com.github.lajospolya.popularVote.controller.geo

import com.github.lajospolya.popularVote.dto.geo.ProvinceAndTerritoryDto
import com.github.lajospolya.popularVote.service.geo.GeoService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("provinces-and-territories")
class ProvinceAndTerritoryController(
    private val geoService: GeoService,
) {
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    fun getProvincesAndTerritories(): Flux<ProvinceAndTerritoryDto> = geoService.getProvincesAndTerritories()
}
