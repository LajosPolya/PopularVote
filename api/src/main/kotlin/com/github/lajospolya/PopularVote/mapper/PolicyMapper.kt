package com.github.lajospolya.PopularVote.mapper

import com.github.lajospolya.PopularVote.dto.CreatePolicyDto
import com.github.lajospolya.PopularVote.dto.PolicyDto
import com.github.lajospolya.PopularVote.entity.Policy
import org.mapstruct.Mapper
import org.mapstruct.MappingConstants

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface PolicyMapper {
    fun entityToDto(policy: Policy): PolicyDto

    fun toEntity(policy: CreatePolicyDto): Policy
}