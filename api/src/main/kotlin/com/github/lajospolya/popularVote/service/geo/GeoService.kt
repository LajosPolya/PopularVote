package com.github.lajospolya.popularVote.service.geo

import com.github.lajospolya.popularVote.dto.geo.GeoDataDto
import com.github.lajospolya.popularVote.dto.geo.ProvinceAndTerritoryDto
import com.github.lajospolya.popularVote.mapper.geo.GeoMapper
import com.github.lajospolya.popularVote.repository.geo.ElectoralDistrictRepository
import com.github.lajospolya.popularVote.repository.geo.MunicipalityRepository
import com.github.lajospolya.popularVote.repository.geo.PostalCodeRepository
import com.github.lajospolya.popularVote.repository.geo.ProvinceAndTerritoryRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class GeoService(
    private val provinceAndTerritoryRepository: ProvinceAndTerritoryRepository,
    private val municipalityRepository: MunicipalityRepository,
    private val electoralDistrictRepository: ElectoralDistrictRepository,
    private val postalCodeRepository: PostalCodeRepository,
    private val geoMapper: GeoMapper,
) {
    fun getProvincesAndTerritories(): Flux<ProvinceAndTerritoryDto> = provinceAndTerritoryRepository.findAll().map { geoMapper.toDto(it) }

    fun getGeoData(): Mono<GeoDataDto> =
        Mono
            .zip(
                provinceAndTerritoryRepository.findAll().map { geoMapper.toDto(it) }.collectList(),
                municipalityRepository.findAll().map { geoMapper.toDto(it) }.collectList(),
                electoralDistrictRepository.findAll().map { geoMapper.toDto(it) }.collectList(),
                postalCodeRepository.findAll().map { geoMapper.toDto(it) }.collectList(),
            ).map { tuple ->
                val provinces = tuple.t1
                val municipalities = tuple.t2
                val electoralDistricts = tuple.t3
                val postalCodes = tuple.t4

                val districtsById = electoralDistricts.associateBy { it.id }

                val nestedPostalCodes =
                    postalCodes.map { postalCode ->
                        postalCode.copy(electoralDistrict = districtsById[postalCode.electoralDistrictId])
                    }

                val postalCodesByMunicipality = nestedPostalCodes.groupBy { it.municipalityId }

                val nestedMunicipalities =
                    municipalities.map { municipality ->
                        municipality.copy(postalCodes = postalCodesByMunicipality[municipality.id] ?: emptyList())
                    }

                val municipalitiesByProvince = nestedMunicipalities.groupBy { it.provinceTerritoryId }

                val districtsByProvince = electoralDistricts.groupBy { it.provinceTerritoryId }

                val nestedProvinces =
                    provinces.map { province ->
                        province.copy(
                            municipalities = municipalitiesByProvince[province.id] ?: emptyList(),
                            electoralDistricts = districtsByProvince[province.id] ?: emptyList(),
                        )
                    }

                GeoDataDto(provincesAndTerritories = nestedProvinces)
            }
}
