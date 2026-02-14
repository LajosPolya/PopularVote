package com.github.lajospolya.popularVote.controller

import com.github.lajospolya.popularVote.dto.OpinionLikeCountDto
import com.github.lajospolya.popularVote.entity.CitizenOpinionLike
import com.github.lajospolya.popularVote.service.CitizenOpinionLikeService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
class CitizenOpinionLikeController(
    private val citizenOpinionLikeService: CitizenOpinionLikeService,
) {
    @PreAuthorize("hasAuthority('SCOPE_read:self') and hasAuthority('SCOPE_read:opinions')")
    @PostMapping("/opinions/{opinionId}/like")
    fun likeOpinion(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable opinionId: Long,
    ): Mono<CitizenOpinionLike> = citizenOpinionLikeService.likeOpinion(jwt.subject, opinionId)

    @PreAuthorize("hasAuthority('SCOPE_read:self') and hasAuthority('SCOPE_read:opinions')")
    @DeleteMapping("/opinions/{opinionId}/like")
    fun unlikeOpinion(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable opinionId: Long,
    ): Mono<Void> = citizenOpinionLikeService.unlikeOpinion(jwt.subject, opinionId)

    @PreAuthorize("hasAuthority('SCOPE_read:self') and hasAuthority('SCOPE_read:opinions')")
    @GetMapping("/citizens/self/liked-opinions")
    fun getLikedOpinions(
        @AuthenticationPrincipal jwt: Jwt,
    ): Flux<Long> = citizenOpinionLikeService.getLikedOpinionIds(jwt.subject)

    @PreAuthorize("hasAuthority('SCOPE_read:opinions')")
    @GetMapping("/opinions/likes/count")
    fun getOpinionLikeCounts(
        @RequestParam opinionIds: List<Long>,
    ): Flux<OpinionLikeCountDto> = citizenOpinionLikeService.getOpinionLikeCounts(opinionIds)
}
