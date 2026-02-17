package com.github.lajospolya.popularVote.service.geo

import com.github.lajospolya.popularVote.dto.geo.GeoDataDto
import com.github.lajospolya.popularVote.mapper.geo.GeoMapper
import com.github.lajospolya.popularVote.repository.geo.FederalElectoralDistrictRepository
import com.github.lajospolya.popularVote.repository.geo.MunicipalityRepository
import com.github.lajospolya.popularVote.repository.geo.PostalCodeRepository
import com.github.lajospolya.popularVote.repository.geo.ProvinceAndTerritoryRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class GeoService(
    private val provinceAndTerritoryRepository: ProvinceAndTerritoryRepository,
    private val municipalityRepository: MunicipalityRepository,
    private val federalElectoralDistrictRepository: FederalElectoralDistrictRepository,
    private val postalCodeRepository: PostalCodeRepository,
    private val geoMapper: GeoMapper,
) {
    fun getGeoData(): Mono<GeoDataDto> =
        Mono.zip(
            provinceAndTerritoryRepository.findAll().map { geoMapper.toDto(it) }.collectList(),
            municipalityRepository.findAll().map { geoMapper.toDto(it) }.collectList(),
            federalElectoralDistrictRepository.findAll().map { geoMapper.toDto(it) }.collectList(),
            postalCodeRepository.findAll().map { geoMapper.toDto(it) }.collectList(),
        ).map { tuple ->
            GeoDataDto(
                provincesAndTerritories = tuple.t1,
                municipalities = tuple.t2,
                federalElectoralDistricts = tuple.t3,
                postalCodes = tuple.t4,
            )
        }
}
