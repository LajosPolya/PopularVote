package com.github.lajospolya.popularVote.controller

import com.github.lajospolya.popularVote.dto.CreatePolicyDto
import com.github.lajospolya.popularVote.dto.PolicyDetailsDto
import com.github.lajospolya.popularVote.dto.PolicyDto
import com.github.lajospolya.popularVote.dto.PolicySummaryDto
import com.github.lajospolya.popularVote.repository.CitizenRepository
import com.github.lajospolya.popularVote.service.PolicyService
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
class PolicyController(
    private val policyService: PolicyService,
    private val citizenRepo: CitizenRepository,
) {
    @PreAuthorize("hasAuthority('SCOPE_read:policies') && hasAuthority('SCOPE_read:self')")
    @RequestMapping("policies", method = [RequestMethod.GET])
    fun getPolicies(
        @AuthenticationPrincipal jwt: Jwt,
    ): Flux<PolicySummaryDto> =
        policyService.getPolicies(jwt.subject)

    @PreAuthorize("hasAuthority('SCOPE_read:policies')")
    @RequestMapping("policies/{id}", method = [RequestMethod.GET])
    fun getPolicy(
        @PathVariable id: Long,
    ): Mono<PolicyDto> = policyService.getPolicy(id)

    @PreAuthorize("hasAuthority('SCOPE_read:policies')")
    @RequestMapping("policies/{id}/details", method = [RequestMethod.GET])
    fun getPolicyDetails(
        @PathVariable id: Long,
    ): Mono<PolicyDetailsDto> = policyService.getPolicyDetails(id)

    @PreAuthorize("hasAuthority('SCOPE_write:policies')")
    @RequestMapping("policies", method = [RequestMethod.POST])
    fun postPolicy(
        @RequestBody policy: CreatePolicyDto,
        @AuthenticationPrincipal jwt: Jwt,
    ): Mono<PolicyDto> =
        citizenRepo
            .findByAuthId(jwt.subject)
            .flatMap { citizen ->
                policyService.createPolicy(policy, citizen.id!!)
            }

    @PreAuthorize("hasAuthority('SCOPE_delete:policies')")
    @RequestMapping("policies/{id}", method = [RequestMethod.DELETE])
    fun deletePolicy(
        @PathVariable id: Long,
    ): Mono<Void> = policyService.deletePolicy(id)

    @PreAuthorize("hasAuthority('SCOPE_write:self')")
    @RequestMapping("policies/{id}/bookmark", method = [RequestMethod.POST])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun postBookmark(
        @PathVariable id: Long,
        @AuthenticationPrincipal jwt: Jwt,
    ): Mono<Void> =
        citizenRepo
            .findByAuthId(jwt.subject)
            .flatMap { citizen ->
                policyService.bookmarkPolicy(id, citizen.id!!)
            }

    @PreAuthorize("hasAuthority('SCOPE_read:self') && hasAuthority('SCOPE_read:policies')")
    @RequestMapping("policies/bookmarks", method = [RequestMethod.GET])
    fun getBookmarks(
        @AuthenticationPrincipal jwt: Jwt,
    ): Flux<PolicySummaryDto> =
        policyService.getBookmarkedPolicies(jwt.subject)

    @PreAuthorize("hasAuthority('SCOPE_read:self') && hasAuthority('SCOPE_read:policies')")
    @RequestMapping("policies/{id}/is-bookmarked", method = [RequestMethod.GET])
    fun isBookmarked(
        @PathVariable id: Long,
        @AuthenticationPrincipal jwt: Jwt,
    ): Mono<Boolean> =
        policyService.isPolicyBookmarked(id, jwt.subject)

    @PreAuthorize("hasAuthority('SCOPE_write:self') && hasAuthority('SCOPE_read:policies')")
    @RequestMapping("policies/{id}/bookmark", method = [RequestMethod.DELETE])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteBookmark(
        @PathVariable id: Long,
        @AuthenticationPrincipal jwt: Jwt,
    ): Mono<Void> =
        citizenRepo
            .findByAuthId(jwt.subject)
            .flatMap { citizen ->
                policyService.unbookmarkPolicy(id, citizen.id!!)
            }
}
