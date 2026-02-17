package com.github.lajospolya.popularVote.dto.geo

data class GeoDataDto(
    val provincesAndTerritories: List<ProvinceAndTerritoryDto>,
    val municipalities: List<MunicipalityDto>,
    val federalElectoralDistricts: List<FederalElectoralDistrictDto>,
    val postalCodes: List<PostalCodeDto>,
)

data class ProvinceAndTerritoryDto(
    val id: Int,
    val name: String,
)

data class MunicipalityDto(
    val id: Int,
    val name: String,
    val provinceTerritoryId: Int,
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
)
