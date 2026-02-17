package com.github.lajospolya.popularVote.controller

import com.github.lajospolya.popularVote.dto.CitizenDto
import com.github.lajospolya.popularVote.dto.CitizenProfileDto
import com.github.lajospolya.popularVote.dto.CitizenSelfDto
import com.github.lajospolya.popularVote.dto.CreateCitizenDto
import com.github.lajospolya.popularVote.dto.DeclarePoliticianDto
import com.github.lajospolya.popularVote.dto.UpdatePostalCodeDto
import com.github.lajospolya.popularVote.service.CitizenService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
class CitizenController(
    private val citizenService: CitizenService,
) {
    @PreAuthorize("hasAuthority('SCOPE_read:citizens')")
    @RequestMapping("citizens", method = [RequestMethod.GET])
    fun getCitizens(): Flux<CitizenDto> = citizenService.getCitizens()

    @PreAuthorize("hasAuthority('SCOPE_read:citizens')")
    @RequestMapping("citizens/politicians", method = [RequestMethod.GET])
    fun getPoliticians(
        @RequestParam(required = false) levelOfPolitics: Long?,
    ): Flux<CitizenDto> = citizenService.getPoliticians(levelOfPolitics)

    @PreAuthorize("hasAuthority('SCOPE_read:verify-politician')")
    @RequestMapping("citizens/verify-politician", method = [RequestMethod.GET])
    fun getPoliticianVerifications(): Flux<CitizenDto> = citizenService.getPoliticianVerifications()

    @PreAuthorize("hasAuthority('SCOPE_read:citizens')")
    @RequestMapping("citizens/{id}", method = [RequestMethod.GET])
    fun getCitizen(
        @PathVariable id: Long,
    ): Mono<CitizenProfileDto> = citizenService.getCitizen(id)

    @PreAuthorize("hasAuthority('SCOPE_read:citizens')")
    @RequestMapping("citizens/search", method = [RequestMethod.GET])
    fun getCitizenByName(
        @RequestParam givenName: String,
        @RequestParam surname: String,
    ): Mono<CitizenDto> = citizenService.getCitizenByName(givenName, surname)

    @PreAuthorize("isAuthenticated()")
    @RequestMapping("citizens/self", method = [RequestMethod.GET])
    fun getSelfByAuthId(
        @AuthenticationPrincipal jwt: Jwt,
    ): Mono<CitizenSelfDto> = citizenService.getCitizenByAuthId(jwt.subject)

    /**
     * Create a user-profile (citizen) for oneself based on info from the JWT access token.
     * The user needs to be authenticated but doesn't need any specific permissions/scopes because this is the first
     * operation the user will perform when logging in, therefore, they won't have any roles or permissions.
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping("citizens/self", method = [RequestMethod.POST])
    fun postCitizen(
        @RequestBody citizen: CreateCitizenDto,
        @AuthenticationPrincipal jwt: Jwt,
    ): Mono<CitizenDto> = citizenService.saveCitizen(citizen, jwt.subject)

    @PreAuthorize("hasAuthority('SCOPE_write:self')")
    @RequestMapping("citizens/self/postal-code", method = [RequestMethod.PUT])
    fun updatePostalCode(
        @RequestBody updatePostalCodeDto: UpdatePostalCodeDto,
        @AuthenticationPrincipal jwt: Jwt,
    ): Mono<CitizenSelfDto> = citizenService.updatePostalCode(jwt.subject, updatePostalCodeDto)

    @PreAuthorize("hasAuthority('SCOPE_write:declare-politician')")
    @RequestMapping("citizens/self/declare-politician", method = [RequestMethod.POST])
    fun declarePolitician(
        @RequestBody declarePoliticianDto: DeclarePoliticianDto,
        @AuthenticationPrincipal jwt: Jwt,
    ): Mono<ResponseEntity<Void>> =
        citizenService
            .declarePolitician(jwt.subject, declarePoliticianDto)
            .thenReturn(ResponseEntity.accepted().build())

    @PreAuthorize("hasAuthority('SCOPE_write:verify-politician')")
    @RequestMapping("citizens/{id}/verify-politician", method = [RequestMethod.PUT])
    fun verifyPolitician(
        @PathVariable id: Long,
    ): Mono<CitizenSelfDto> = citizenService.verifyPolitician(id)

    @PreAuthorize("hasAuthority('SCOPE_delete:citizens')")
    @RequestMapping("citizens/{id}", method = [RequestMethod.DELETE])
    fun deleteCitizen(
        @PathVariable id: Long,
    ): Mono<Void> = citizenService.deleteCitizen(id)
}
