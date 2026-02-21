package com.github.lajospolya.popularVote.mapper.geo

import com.github.lajospolya.popularVote.dto.geo.ElectoralDistrictDto
import com.github.lajospolya.popularVote.dto.geo.MunicipalityDto
import com.github.lajospolya.popularVote.dto.geo.PostalCodeDto
import com.github.lajospolya.popularVote.dto.geo.ProvinceAndTerritoryDto
import com.github.lajospolya.popularVote.entity.geo.ElectoralDistrict
import com.github.lajospolya.popularVote.entity.geo.Municipality
import com.github.lajospolya.popularVote.entity.geo.PostalCode
import com.github.lajospolya.popularVote.entity.geo.ProvinceAndTerritory
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface GeoMapper {
    @Mapping(target = "municipalities", ignore = true)
    @Mapping(target = "electoralDistricts", ignore = true)
    fun toDto(provinceAndTerritory: ProvinceAndTerritory): ProvinceAndTerritoryDto

    @Mapping(target = "postalCodes", ignore = true)
    fun toDto(municipality: Municipality): MunicipalityDto

    fun toDto(electoralDistrict: ElectoralDistrict): ElectoralDistrictDto

    @Mapping(target = "electoralDistrict", ignore = true)
    fun toDto(postalCode: PostalCode): PostalCodeDto
}
