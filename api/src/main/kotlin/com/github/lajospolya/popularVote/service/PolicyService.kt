package com.github.lajospolya.popularVote.service

import com.github.lajospolya.popularVote.controller.exception.ResourceNotFoundException
import com.github.lajospolya.popularVote.dto.CreatePolicyDto
import com.github.lajospolya.popularVote.dto.PolicyDto
import com.github.lajospolya.popularVote.entity.Policy
import com.github.lajospolya.popularVote.mapper.PolicyMapper
import com.github.lajospolya.popularVote.repository.PolicyRepository
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
        return getPolicyElseThrowResourceNotFound(id).map(policyMapper::entityToDto)
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
