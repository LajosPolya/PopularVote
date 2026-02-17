package com.github.lajospolya.popularVote.dto.geo

data class GeoDataDto(
    val provincesAndTerritories: List<ProvinceAndTerritoryDto>,
)

data class ProvinceAndTerritoryDto(
    val id: Int,
    val name: String,
    val municipalities: List<MunicipalityDto>? = emptyList(),
)

data class MunicipalityDto(
    val id: Int,
    val name: String,
    val provinceTerritoryId: Int,
    val postalCodes: List<PostalCodeDto>? = emptyList(),
)

data class FederalElectoralDistrictDto(
    val id: Int,
    val name: String,
    val code: Int,
    val municipalityId: Int,
)

data class PostalCodeDto(
    val id: Int,
    val name: String,
    val code: Int,
    val municipalityId: Int,
    val federalElectoralDistrictId: Int,
    val federalElectoralDistrict: FederalElectoralDistrictDto? = null,
)
