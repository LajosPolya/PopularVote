package com.github.lajospolya.popularVote.controller.geo

import com.github.lajospolya.popularVote.dto.geo.GeoDataDto
import com.github.lajospolya.popularVote.service.geo.GeoService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class GeoController(
    private val geoService: GeoService,
) {
    @RequestMapping("geo-data", method = [RequestMethod.GET])
    fun getGeoData(): Mono<GeoDataDto> = geoService.getGeoData()
}
