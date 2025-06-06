package com.github.lajospolya.popularVote.controller

import com.github.lajospolya.popularVote.dto.CreatePolicyDto
import com.github.lajospolya.popularVote.dto.PolicyDto
import com.github.lajospolya.popularVote.service.PolicyService
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
class PolicyController(
    private val policyService: PolicyService,
) {
    @RequestMapping("policies", method = [RequestMethod.GET])
    fun getPolicies(): Flux<PolicyDto> {
        return policyService.getPolicies()
    }

    @RequestMapping("policies/{id}", method = [RequestMethod.GET])
    fun getPolicy(
        @PathVariable id: Long,
    ): Mono<PolicyDto> {
        return policyService.getPolicy(id)
    }

    @RequestMapping("policies", method = [RequestMethod.POST])
    fun postPolicy(
        @RequestBody policy: CreatePolicyDto,
    ): Mono<PolicyDto> {
        return policyService.createPolicy(policy)
    }

    @RequestMapping("policies/{id}", method = [RequestMethod.DELETE])
    fun deletePolicy(
        @PathVariable id: Long,
    ): Mono<Void> {
        return policyService.deletePolicy(id)
    }
}
