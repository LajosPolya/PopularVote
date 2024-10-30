package com.github.lajospolya.PopularVote.mapper

import com.github.lajospolya.PopularVote.dto.CreateOpinionDto
import com.github.lajospolya.PopularVote.dto.OpinionDto
import com.github.lajospolya.PopularVote.entity.Opinion
import org.mapstruct.Mapper
import org.mapstruct.MappingConstants

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface OpinionMapper {

    fun toDto(opinion: Opinion): OpinionDto

    fun toEntity(opinion: CreateOpinionDto): Opinion
}