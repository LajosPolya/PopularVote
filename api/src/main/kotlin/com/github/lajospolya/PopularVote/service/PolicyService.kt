package com.github.lajospolya.PopularVote.service

import com.github.lajospolya.PopularVote.controller.exception.ResourceNotFoundException
import com.github.lajospolya.PopularVote.dto.CreatePolicyDto
import com.github.lajospolya.PopularVote.dto.PolicyDto
import com.github.lajospolya.PopularVote.entity.Citizen
import com.github.lajospolya.PopularVote.entity.Policy
import com.github.lajospolya.PopularVote.mapper.PolicyMapper
import com.github.lajospolya.PopularVote.repository.PolicyRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class PolicyService(
    val policyRepo: PolicyRepository,
    val policyMapper: PolicyMapper,
) {

    fun getPolicies(): Flux<PolicyDto> {
        return policyRepo.findAll().map(policyMapper::entityToDto)
    }

    fun getPolicy(id: Long): Mono<PolicyDto> {
        return policyRepo.findById(id).map(policyMapper::entityToDto)
    }

    fun createPolicy(policyDto: CreatePolicyDto): Mono<PolicyDto> {
        val policy = policyMapper.toEntity(policyDto)
        return policyRepo.save(policy).map(policyMapper::entityToDto)
    }

    fun deletePolicy(id: Long): Mono<Void> {
        return getPolicyElseThrowResourceNotFound(id).flatMap(policyRepo::delete)
    }

    private fun getPolicyElseThrowResourceNotFound(id: Long): Mono<Policy> {
        return policyRepo.findById(id)
            .switchIfEmpty {
                Mono.error(ResourceNotFoundException())
            }
    }
}