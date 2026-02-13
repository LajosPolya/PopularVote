package com.github.lajospolya.popularVote.controller

import com.github.lajospolya.popularVote.dto.CitizenDto
import com.github.lajospolya.popularVote.dto.CreatePoliticalPartyDto
import com.github.lajospolya.popularVote.dto.PoliticalPartyDto
import com.github.lajospolya.popularVote.dto.PolicySummaryDto
import com.github.lajospolya.popularVote.service.PoliticalPartyService
import com.github.lajospolya.popularVote.service.PolicyService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("political-parties")
class PoliticalPartyController(
    private val politicalPartyService: PoliticalPartyService,
    private val policyService: PolicyService,
) {
    @PreAuthorize("hasAuthority('SCOPE_read:political-parties')")
    @GetMapping
    fun getPoliticalParties(): Flux<PoliticalPartyDto> =
        politicalPartyService.getPoliticalParties()

    @PreAuthorize("hasAuthority('SCOPE_read:political-parties')")
    @GetMapping("/{id}")
    fun getPoliticalParty(@PathVariable id: Int): Mono<PoliticalPartyDto> =
        politicalPartyService.getPoliticalParty(id)
    
    @PreAuthorize("hasAuthority('SCOPE_read:political-parties') and hasAuthority('SCOPE_read:citizens')")
    @GetMapping("/{id}/members")
    fun getPoliticalPartyMembers(@PathVariable id: Int): Flux<CitizenDto> =
        politicalPartyService.getPoliticalPartyMembers(id)

    @PreAuthorize("hasAuthority('SCOPE_read:political-parties') and hasAuthority('SCOPE_read:policies')")
    @GetMapping("/{id}/policies")
    fun getPoliticalPartyPolicies(
        @PathVariable id: Int,
        @AuthenticationPrincipal jwt: Jwt?
    ): Flux<PolicySummaryDto> =
        policyService.getPoliciesByPoliticalPartyId(id, jwt?.subject)

    @PreAuthorize("hasAuthority('SCOPE_write:political-parties')")
    @PostMapping
    fun postPoliticalParty(@RequestBody politicalParty: CreatePoliticalPartyDto): Mono<PoliticalPartyDto> =
        politicalPartyService.createPoliticalParty(politicalParty)

    @PreAuthorize("hasAuthority('SCOPE_write:political-parties')")
    @PutMapping("/{id}")
    fun putPoliticalParty(
        @PathVariable id: Int,
        @RequestBody politicalParty: CreatePoliticalPartyDto
    ): Mono<PoliticalPartyDto> =
        politicalPartyService.updatePoliticalParty(id, politicalParty)

    @PreAuthorize("hasAuthority('SCOPE_delete:political-parties')")
    @DeleteMapping("/{id}")
    fun deletePoliticalParty(@PathVariable id: Int): Mono<Void> =
        politicalPartyService.deletePoliticalParty(id)
}
