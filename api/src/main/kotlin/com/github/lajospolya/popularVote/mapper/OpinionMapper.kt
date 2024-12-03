package com.github.lajospolya.popularVote.mapper

import com.github.lajospolya.popularVote.dto.CreateOpinionDto
import com.github.lajospolya.popularVote.dto.OpinionDto
import com.github.lajospolya.popularVote.entity.Opinion
import org.mapstruct.Mapper
import org.mapstruct.MappingConstants

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface OpinionMapper {
    fun toDto(opinion: Opinion): OpinionDto

    fun toEntity(opinion: CreateOpinionDto): Opinion
}
