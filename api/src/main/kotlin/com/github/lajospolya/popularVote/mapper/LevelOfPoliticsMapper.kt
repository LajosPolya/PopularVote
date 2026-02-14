package com.github.lajospolya.popularVote.mapper

import com.github.lajospolya.popularVote.dto.LevelOfPoliticsDto
import com.github.lajospolya.popularVote.entity.LevelOfPolitics
import org.mapstruct.Mapper
import org.mapstruct.MappingConstants

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface LevelOfPoliticsMapper {
    fun toDto(levelOfPolitics: LevelOfPolitics): LevelOfPoliticsDto
}
