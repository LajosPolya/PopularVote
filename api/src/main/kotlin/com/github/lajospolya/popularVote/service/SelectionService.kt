package com.github.lajospolya.popularVote.service

import com.github.lajospolya.popularVote.controller.exception.ResourceNotFoundException
import com.github.lajospolya.popularVote.entity.PollSelection
import com.github.lajospolya.popularVote.repository.SelectionRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class SelectionService(
    private val selectionRepository: SelectionRepository,
) {
    fun getSelection(id: Long): Mono<PollSelection> = getSelectionElseThrowResourceNotFound(id)

    private fun getSelectionElseThrowResourceNotFound(id: Long): Mono<PollSelection> =
        selectionRepository
            .findById(id)
            .switchIfEmpty {
                Mono.error(ResourceNotFoundException())
            }
}
