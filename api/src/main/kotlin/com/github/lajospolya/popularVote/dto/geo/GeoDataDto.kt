package com.github.lajospolya.popularVote.dto.geo

data class GeoDataDto(
    val provincesAndTerritories: List<ProvinceAndTerritoryDto>,
)

data class ProvinceAndTerritoryDto(
    val id: Int,
    val name: String,
    val municipalities: List<MunicipalityDto>? = emptyList(),
    val electoralDistricts: List<ElectoralDistrictDto>? = emptyList(),
)

data class MunicipalityDto(
    val id: Int,
    val name: String,
    val provinceTerritoryId: Int,
    val postalCodes: List<PostalCodeDto>? = emptyList(),
)

data class ElectoralDistrictDto(
    val id: Int,
    val name: String,
    val code: Int,
    val provinceTerritoryId: Int,
    val levelOfPoliticsId: Int,
)

data class PostalCodeDto(
    val id: Int,
    val name: String,
    val code: Int,
    val municipalityId: Int,
    val electoralDistrictId: Int,
    val electoralDistrict: ElectoralDistrictDto? = null,
)
