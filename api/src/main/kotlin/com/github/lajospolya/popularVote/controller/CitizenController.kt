package com.github.lajospolya.popularVote.controller

import com.github.lajospolya.popularVote.dto.CitizenDto
import com.github.lajospolya.popularVote.dto.CitizenSelfDto
import com.github.lajospolya.popularVote.dto.CreateCitizenDto
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
    @RequestMapping("citizens/{id}", method = [RequestMethod.GET])
    fun getCitizen(
        @PathVariable id: Long,
    ): Mono<CitizenDto> = citizenService.getCitizen(id)

    @PreAuthorize("hasAuthority('SCOPE_read:citizens')")
    @RequestMapping("citizens/search", method = [RequestMethod.GET])
    fun getCitizenByName(
        @RequestParam givenName: String,
        @RequestParam surname: String,
    ): Mono<CitizenDto> = citizenService.getCitizenByName(givenName, surname)

    @PreAuthorize("hasAuthority('SCOPE_read:self')")
    @RequestMapping("citizens/self", method = [RequestMethod.GET])
    fun getSelfByAuthId(
        @AuthenticationPrincipal jwt: Jwt,
    ): Mono<CitizenSelfDto> = citizenService.getCitizenByAuthId(jwt.subject)

    @PreAuthorize("isAuthenticated()")
    @RequestMapping("citizens/self", method = [RequestMethod.HEAD])
    fun checkCitizenExistsByAuthId(
        @AuthenticationPrincipal jwt: Jwt,
    ): Mono<ResponseEntity<Void>> =
        citizenService
            .checkCitizenExistsByAuthId(jwt.subject)
            .map { exists ->
                if (exists) {
                    ResponseEntity.noContent().build<Void>()
                } else {
                    ResponseEntity.notFound().build<Void>()
                }
            }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping("citizens/self", method = [RequestMethod.POST])
    fun postCitizen(
        @RequestBody citizen: CreateCitizenDto,
        @AuthenticationPrincipal jwt: Jwt,
    ): Mono<CitizenDto> = citizenService.saveCitizen(citizen, jwt.subject)

    @PreAuthorize("hasAuthority('SCOPE_delete:citizens')")
    @RequestMapping("citizens/{id}", method = [RequestMethod.DELETE])
    fun deleteCitizen(
        @PathVariable id: Long,
    ): Mono<Void> = citizenService.deleteCitizen(id)
}
