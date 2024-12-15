package com.github.lajospolya.popularVote.mapper

import com.github.lajospolya.popularVote.dto.CreatePolicyDto
import com.github.lajospolya.popularVote.dto.PolicyDto
import com.github.lajospolya.popularVote.entity.Policy
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface PolicyMapper {
    fun entityToDto(policy: Policy): PolicyDto

    @Mapping(target = "id", ignore = true)
    fun toEntity(policy: CreatePolicyDto): Policy
}
