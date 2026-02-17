package com.github.lajospolya.popularVote.repository.geo

import com.github.lajospolya.popularVote.entity.geo.FederalElectoralDistrict
import com.github.lajospolya.popularVote.entity.geo.Municipality
import com.github.lajospolya.popularVote.entity.geo.PostalCode
import com.github.lajospolya.popularVote.entity.geo.ProvinceAndTerritory
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ProvinceAndTerritoryRepository : ReactiveCrudRepository<ProvinceAndTerritory, Int>

@Repository
interface MunicipalityRepository : ReactiveCrudRepository<Municipality, Int>

@Repository
interface FederalElectoralDistrictRepository : ReactiveCrudRepository<FederalElectoralDistrict, Int>

@Repository
interface PostalCodeRepository : ReactiveCrudRepository<PostalCode, Int>
